package com.nuecho.genesys.cli

import com.genesyslab.platform.reporting.protocol.statserver.DnActions
import com.nuecho.genesys.cli.commands.agent.mockAgentStatus
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTimeZone
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.util.TimeZone

class GenesysExtensionsTest {
    @Test
    fun `AgentStatus to console string output should be correct`() {
        val status = mockAgentStatus(
            agentId = "test",
            placeId = "testPlace",
            switchId = "testSwitch",
            dnId = "1234",
            status = DnActions.LoggedIn
        )

        assertThat(
            status.toConsoleString(),
            equalTo(
                """
                |Agent (test):
                |  Status: LoggedIn
                |  Place : testPlace
                |  DNs   : [DN: 1234 - Switch: testSwitch]
                """.trimMargin()
            )
        )
    }

    @Test
    fun `CFGTimeZone to ZoneID should be correct`() {
        var cfgTimeZone = mockCfgTimeZone("GMT")
        assertThat(TimeZone.getTimeZone(ZoneId.of("GMT", ZoneId.SHORT_IDS)).getDisplayName(false, TimeZone.SHORT), equalTo(cfgTimeZone.toTimeZoneId()))

        cfgTimeZone = mockCfgTimeZone("ECT")
        assertThat(TimeZone.getTimeZone(ZoneId.of("ECT", ZoneId.SHORT_IDS)).getDisplayName(false, TimeZone.SHORT), equalTo(cfgTimeZone.toTimeZoneId()))
    }
}
