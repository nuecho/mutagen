package com.nuecho.genesys.cli.commands.audio

import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test

private const val USAGE_PREFIX = "Usage: audio [-?]"

class AudioCommandTest {
    @Test
    fun `executing Audio command with no argument should print usage`() {
        val output = execute("audio")
        assertThat(output, startsWith(USAGE_PREFIX))
    }

    @Test
    fun `executing Audio command with -h argument should print usage`() {
        val output = execute("audio", "-h")
        assertThat(output, startsWith(USAGE_PREFIX))
    }
}
