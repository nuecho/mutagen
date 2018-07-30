package com.nuecho.genesys.cli.commands.config.validate

import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test

private const val USAGE_PREFIX = "Usage: validate [-?]"

class ValidateCommandTest {

    @Test
    fun `executing Export with -h argument should print usage`() {
        val output = execute("config", "validate", "-h")

        assertThat(output, startsWith(USAGE_PREFIX))
    }
}
