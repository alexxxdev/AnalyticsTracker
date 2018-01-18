package com.github.alexxxdev.analyticstracker

import com.squareup.kotlinpoet.*
import java.io.IOException
import java.util.ArrayList
import java.util.LinkedHashSet
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import javax.tools.StandardLocation
import kotlin.collections.HashMap

/**
 * Created by aderendyaev on 12.12.17.
 */

class AnalyticsProcessor : AbstractProcessor() {

    private val nameTracker = "Tracker"
    private val packageTracker = "com.github.alexxxdev.analyticstracker"

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        val supportTypes = LinkedHashSet<String>()
        supportTypes.add(AnalyticsAttr::class.java.canonicalName)
        supportTypes.add(AnalyticsEnumAttr::class.java.canonicalName)
        supportTypes.add(Analytics::class.java.canonicalName)
        return supportTypes
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        try {
            val rootToNested = HashMap<Element, MutableList<Element>?>()
            var handler: Set<Element> = emptySet()
            for (annotation in annotations) {
                if (annotation.simpleName.toString() == Analytics::class.java.simpleName) {
                    handler = roundEnv.getElementsAnnotatedWith(annotation) as Set<Element>
                } else {
                    val annotatedElements = roundEnv.getElementsAnnotatedWith(annotation)
                    for (element in annotatedElements) {
                        val root = element.enclosingElement

                        val nested: MutableList<Element>? = rootToNested[root]
                        if (nested == null) {
                            val arrayList = ArrayList<Element>()
                            arrayList.add(element)
                            rootToNested.put(root, arrayList)
                        }
                        nested?.add(element)
                    }
                }
            }

            if (rootToNested.isEmpty()) return false

            val builder = FileSpec.builder(packageTracker, nameTracker)
            builder.indent("\t")

            val clazz = TypeSpec.classBuilder(nameTracker)
                    .addModifiers(KModifier.OPEN)
                    .addProperty(PropertySpec.varBuilder("handlers", ParameterizedTypeName.get(ARRAY, WildcardTypeName.subtypeOf(AnalyticsHandler::class.asTypeName())), KModifier.PRIVATE)
                            .build())
                    .addInitializerBlock(createFunInit(handler))
                    .addFunction(createFunSendAll())
                    .addFunction(createFunSend())

            rootToNested.forEach { (element, mutableList) ->
                mutableList?.let {
                    clazz.addFunction(createFunSend(element, mutableList))
                }
            }

            builder.addType(clazz.build())

            val writer = processingEnv.filer.createResource(StandardLocation.SOURCE_OUTPUT, packageTracker, "$nameTracker.kt").openWriter()

            writer.append(builder.build().toString())
            writer.flush()
            writer.close()

        } catch (e: IOException) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Exception: " + e)
        }

        return true
    }

    private fun createFunInit(handler: Set<Element>): CodeBlock {
        val initBlock = CodeBlock.builder()
                .addStatement("handlers = arrayOf(")

        handler.forEachIndexed { index, item ->
            if (index < handler.size - 1) {
                initBlock.addStatement("%T(),", item.asType().asTypeName())
            } else {
                initBlock.addStatement("%T()", item.asType().asTypeName())
            }
        }
        initBlock.addStatement(")")
        return initBlock.build()
    }

    private fun createFunSend(element: Element, mutableList: MutableList<Element>): FunSpec {
        val function = FunSpec.builder("send")
                .addParameter("event", String::class.asClassName())
                .addParameter(
                        ParameterSpec.builder(element.simpleName.toString().toLowerCase(), element.asType().asTypeName())
                                .build()
                )
                .addStatement("val attrs = mapOf(")

        mutableList.forEachIndexed { index, item ->
            val annotation = item.getAnnotation(AnalyticsAttr::class.java)
            val annotationEnum = item.getAnnotation(AnalyticsEnumAttr::class.java)

            var suffix = if (annotationEnum != null) "?.name" else ""

            val value = if (annotation != null) {
                item.getAnnotation(AnalyticsAttr::class.java)?.value ?: item.simpleName.toString()
            } else {
                item.getAnnotation(AnalyticsEnumAttr::class.java)?.value ?: item.simpleName.toString()
            }

            if (index < mutableList.size - 1) suffix += ","

            function.addStatement("\"$value\" to ${element.simpleName.toString().toLowerCase()}.${item.simpleName}$suffix")
        }

        function.addStatement(")")
        function.addStatement("sendAll(event, attrs)")
        return function.build()
    }

    private fun createFunSend(): FunSpec {
        return FunSpec.builder("send")
                .addModifiers(KModifier.PUBLIC)
                .addParameter("event", String::class.asClassName())
                .addParameter(
                        ParameterSpec.builder("attrs", ParameterizedTypeName.get(Map::class.asClassName(), String::class.asClassName(), ANY.asNullable()))
                                .defaultValue("emptyMap()")
                                .build()
                )
                .addStatement("sendAll(event, attrs)")
                .build()
    }

    private fun createFunSendAll(): FunSpec {
        return FunSpec.builder("sendAll")
                .addModifiers(KModifier.PRIVATE)
                .addParameter("event", String::class.asClassName())
                .addParameter("attrs", ParameterizedTypeName.get(Map::class.asClassName(), String::class.asClassName(), ANY.asNullable()))
                .beginControlFlow("handlers.forEach")
                .addStatement("it.send(event, attrs.filter { it.value != null })")
                .endControlFlow()
                .build()
    }
}
