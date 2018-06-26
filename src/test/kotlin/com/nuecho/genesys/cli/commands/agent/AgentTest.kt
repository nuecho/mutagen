package com.nuecho.genesys.cli.commands.agent

import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val USAGE_PREFIX = "Usage: agent [-?]"

class AgentTest {
    @Test
    fun `executing Agent with no argument should print usage`() {
        val output = execute("agent")
        assertTrue(output.startsWith(USAGE_PREFIX))
    }

    @Test
    fun `executing Agent with -h argument should print usage`() {
        val output = execute("agent", "-h")
        assertTrue(output.startsWith(USAGE_PREFIX))
    }
}
