### AnalyticsTracker

[![](https://jitpack.io/v/alexxxdev/AnalyticsTracker.svg)](https://jitpack.io/#alexxxdev/AnalyticsTracker)

Annotation based tracking handler

### Using
```Groovy
buildscript {
  repositories {
	  maven { url 'https://jitpack.io' }
	}
  dependencies{
    implementation 'com.github.alexxxdev:AnalyticsTracker:1.1.0'
    kapt 'com.github.alexxxdev:AnalyticsTracker:1.1.0'
  }
}
```
```Kotlin
@Analytics
class AnyAnalytics : AnalyticsHandler {
    override fun send(name: String, attrs: Map<String, Any?>) {
        ...
    }
}
```
```Kotlin
class Foo {
    @AnalyticsAttr
    val id:Int,
    @AnalyticsAttr("name")
    var name:String
    ...
}
```
```Kotlin
val foo = Foo()
AnalyticsTracker.send("event name", foo)
```

or

```Kotlin
AnalyticsTracker.send("event name")
AnalyticsTracker.send("event name", mapOf(...))
```
