package com.nuecho.genesys.cli.commands.agent

import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import io.kotlintest.matchers.should
import io.kotlintest.matchers.startWith
import io.kotlintest.specs.StringSpec

private const val USAGE_PREFIX = "Usage: agent [-?]"

class AgentTest : StringSpec() {
    init {
        "executing Agent with no argument should print usage" {
            val output = execute("agent")
            output should startWith(USAGE_PREFIX)
        }

        "executing Agent with -h argument should print usage" {
            val output = execute("agent", "-h")
            output should startWith(USAGE_PREFIX)
        }
    }
}
