package com.nuecho.genesys.cli.commands.config

import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test

private const val USAGE_PREFIX = "Usage: config [-?]"

class ConfigTest {
    @Test
    fun `executing Config with no argument should print usage`() {
        val output = execute("config")
        assertThat(output, startsWith(USAGE_PREFIX))
    }

    @Test
    fun `executing Config with -h argument should print usage`() {
        val output = execute("config", "-h")
        assertThat(output, startsWith(USAGE_PREFIX))
    }
}
