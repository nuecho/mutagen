package com.nuecho.genesys.cli.commands.audio

import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val USAGE_PREFIX = "Usage: audio [-?]"

class AudioCommandTest {
    @Test
    fun `executing Audio command with no argument should print usage`() {
        val output = execute("audio")
        assertTrue(output.startsWith(USAGE_PREFIX))
    }

    @Test
    fun `executing Audio command with -h argument should print usage`() {
        val output = execute("audio", "-h")
        assertTrue(output.startsWith(USAGE_PREFIX))
    }
}
