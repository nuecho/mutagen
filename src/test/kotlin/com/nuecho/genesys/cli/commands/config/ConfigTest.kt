package com.nuecho.genesys.cli.commands.config

import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val USAGE_PREFIX = "Usage: config [-?]"

class ConfigTest {
    @Test
    fun `executing Config with no argument should print usage`() {
        val output = execute("config")
        assertTrue(output.startsWith(USAGE_PREFIX))
    }

    @Test
    fun `executing Config with -h argument should print usage`() {
        val output = execute("config", "-h")
        assertTrue(output.startsWith(USAGE_PREFIX))
    }
}
