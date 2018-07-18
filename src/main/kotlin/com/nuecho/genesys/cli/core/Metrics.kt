package com.nuecho.genesys.cli.core

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.json.MetricsModule
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.OutputStream
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

object Metrics {
    private val metricsRegistry = MetricRegistry()
    private val metricsMapper: ObjectMapper = jacksonObjectMapper().registerModule(
        MetricsModule(SECONDS, MILLISECONDS, false)
    )

    fun <T> time(name: String, function: () -> T): T {
        val timer = metricsRegistry.timer(name).time()

        try {
            return function()
        } finally {
            timer.stop()
        }
    }

    fun timers() = metricsRegistry.timers

    fun output(output: OutputStream) = jsonGeneratorBase(output).setCodec(metricsMapper).writeObject(metricsRegistry)
}
