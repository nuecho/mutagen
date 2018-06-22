package com.nuecho.genesys.cli

import com.genesyslab.platform.reporting.protocol.statserver.DnActions
import com.nuecho.genesys.cli.commands.agent.mockAgentStatus
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks
import org.junit.jupiter.api.Assertions.assertEquals
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

        assertEquals(
            """
                |Agent (test):
                |  Status: LoggedIn
                |  Place : testPlace
                |  DNs   : [DN: 1234 - Switch: testSwitch]
                """.trimMargin(),
            status.toConsoleString()
        )
    }

    @Test
    fun `CFGTimeZone to ZoneID should be correct`() {
        var cfgTimeZone = ConfigurationObjectMocks.mockCfgTimeZone("GMT")
        assertEquals(TimeZone.getTimeZone(ZoneId.of("GMT", ZoneId.SHORT_IDS)).getDisplayName(false, TimeZone.SHORT), cfgTimeZone.toTimeZoneId())

        cfgTimeZone = ConfigurationObjectMocks.mockCfgTimeZone("ECT")
        assertEquals(cfgTimeZone.toTimeZoneId(), TimeZone.getTimeZone(ZoneId.of("ECT", ZoneId.SHORT_IDS)).getDisplayName(false, TimeZone.SHORT))
    }
}
