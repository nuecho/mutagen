package com.nuecho.genesys.cli

import com.genesyslab.platform.reporting.protocol.statserver.DnActions
import com.nuecho.genesys.cli.commands.agent.mockAgentStatus
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import java.time.ZoneId
import java.util.TimeZone

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

        "CFGTimeZone to ZoneID should be correct" {
            var cfgTimeZone = ConfigurationObjectMocks.mockCfgTimeZone("GMT")
            cfgTimeZone.toTimeZoneId() shouldBe TimeZone.getTimeZone(ZoneId.of("GMT", ZoneId.SHORT_IDS)).getDisplayName(false, TimeZone.SHORT)

            cfgTimeZone = ConfigurationObjectMocks.mockCfgTimeZone("ECT")
            cfgTimeZone.toTimeZoneId() shouldBe TimeZone.getTimeZone(ZoneId.of("ECT", ZoneId.SHORT_IDS)).getDisplayName(false, TimeZone.SHORT)
        }
    }
}
