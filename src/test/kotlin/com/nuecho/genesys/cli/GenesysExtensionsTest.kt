package com.nuecho.genesys.cli

import com.genesyslab.platform.reporting.protocol.statserver.DnActions
import com.nuecho.genesys.cli.commands.agent.mockAgentStatus
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

class GenesysExtensionsTest : StringSpec() {
    init {
        "AgentStatus to console string output should be correct" {
            val status = mockAgentStatus(
                agentId = "test",
                placeId = "testPlace",
                switchId = "testSwitch",
                dnId = "1234",
                status = DnActions.LoggedIn
            )

            status.toConsoleString() shouldBe """
                |Agent (test):
                |  Status: LoggedIn
                |  Place : testPlace
                |  DNs   : [DN: 1234 - Switch: testSwitch]
                """.trimMargin()
        }
    }
}
