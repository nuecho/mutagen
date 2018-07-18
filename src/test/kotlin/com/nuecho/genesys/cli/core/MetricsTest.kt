package com.nuecho.genesys.cli.core

import com.nuecho.genesys.cli.core.Metrics.time
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class MetricsTest {
    @Test
    fun `time should add a Timer metric`() {
        val timerCount = Metrics.timers().size
        time("test") {}
        assertThat(Metrics.timers().size, `is`(timerCount + 1))
    }

    @Test
    fun `metrics should property serialize and deserialize`() {
        val outputStream = ByteArrayOutputStream()
        Metrics.output(outputStream)

        val jsonMetrics = defaultJsonObjectMapper().readTree(outputStream.toByteArray())
        assertThat(jsonMetrics.isObject, `is`(true))
        assertThat(jsonMetrics.has("timers"), `is`(true))
    }
}
