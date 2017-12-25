package com.github.alexxxdev.analyticstracker

/**
 * Created by aderendyaev on 12.12.17.
 */

interface AnalyticsHandler {
    fun send(name: String, attrs: Map<String, Any?>)
}
