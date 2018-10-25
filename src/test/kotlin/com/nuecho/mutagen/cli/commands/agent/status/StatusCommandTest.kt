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

package com.nuecho.mutagen.cli.commands.agent.status

import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.commons.protocol.MessageHandler
import com.genesyslab.platform.reporting.protocol.statserver.AgentStatus
import com.genesyslab.platform.reporting.protocol.statserver.DnActions
import com.genesyslab.platform.reporting.protocol.statserver.events.EventInfo
import com.genesyslab.platform.reporting.protocol.statserver.events.EventStatisticInvalid
import com.genesyslab.platform.reporting.protocol.statserver.events.EventStatisticOpened
import com.genesyslab.platform.reporting.protocol.statserver.requests.RequestOpenStatisticEx
import com.nuecho.mutagen.cli.CliOutputCaptureWrapper.execute
import com.nuecho.mutagen.cli.TestResources
import com.nuecho.mutagen.cli.commands.agent.mockAgentStatus
import com.nuecho.mutagen.cli.services.StatService
import com.nuecho.mutagen.cli.services.StatServiceException
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.experimental.launch
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

private const val RESOURCE_PREFIX = "commands/status"
private const val MISSING_REQUIRED_OPTIONS_PATH = "$RESOURCE_PREFIX/missing-required-options.txt"
private const val USAGE_PREFIX_PATH = "$RESOURCE_PREFIX/usage.txt"
private const val TEST_TIMEOUT = 1L

class StatusCommandTest {
    @Test
    fun `executing Status with no arguments should print usage`() {
        val expectedOutput = TestResources.getTestResource(MISSING_REQUIRED_OPTIONS_PATH).readText()
        val output = execute("agent", "status")
        assertEquals(expectedOutput, output)
    }

    @Test
    fun `executing Status with -h argument should print usage`() {
        val expectedOutput = TestResources.getTestResource(USAGE_PREFIX_PATH).readText()
        val output = execute("agent", "status", "-h")
        assertEquals(expectedOutput, output)
    }

    @Test
    fun `executing getAgentStatus should return AgentStatus`() {
        val agentId = "test"
        val agent = mockAgent(agentId)
        val agentStatus = DnActions.WaitForNextCall

        val statServiceMock = mockStatService(mockAgentStatus(agentId, status = agentStatus))

        val output = Status.getAgentStatus(statServiceMock, agent, TEST_TIMEOUT)

        assertThat(output.agentId, equalTo(agentId))
        assertThat(output.status, equalTo(agentStatus.asInteger()))
    }

    @Test
    fun `executing getAgentStatus should throw if statistic fails`() {
        val agentId = "test"
        val agent = mockAgent(agentId)

        val statServiceMock = mockStatService(mockAgentStatus(agentId))

        // Fail EventStatistic creation
        every { statServiceMock.request(ofType(RequestOpenStatisticEx::class)) } returns EventStatisticInvalid.create()

        assertThrows(StatServiceException::class.java) {
            Status.getAgentStatus(statServiceMock, agent, TEST_TIMEOUT)
        }
    }

    @Test
    fun `executing getAgentStatus should throw if AgentStatus is never received`() {
        val agentId = "test"
        val agent = mockAgent(agentId)

        val statServiceMock = mockStatService(mockAgentStatus(agentId))

        // Do not send AgentStatus
        every { statServiceMock.request(ofType(RequestOpenStatisticEx::class)) } returns EventStatisticOpened.create()

        assertThrows(StatServiceException::class.java) {
            Status.getAgentStatus(statServiceMock, agent, TEST_TIMEOUT)
        }
    }
}

private fun mockAgent(employeeId: String): CfgPerson {
    val agent = mockk<CfgPerson>()
    every { agent.employeeID } returns employeeId
    every { agent.tenant.name } returns "testTenant"
    return agent
}

private fun mockStatService(agentStatus: AgentStatus): StatService {
    val messageHandler = slot<MessageHandler>()

    val service = mockk<StatService>()
    every { service.setMessageHandler(capture(messageHandler)) } just Runs
    every { service.open() } just Runs
    every { service.closeStatistic(any()) } just Runs
    every { service.close() } just Runs

    val event = mockk<EventInfo>()
    every { event.stateValue } returns agentStatus

    every { service.request(ofType(RequestOpenStatisticEx::class)) } answers {
        launch { messageHandler.captured.onMessage(event) }
        EventStatisticOpened.create()
    }

    return service
}
