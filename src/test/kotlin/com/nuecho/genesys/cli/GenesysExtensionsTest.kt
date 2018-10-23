/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
