package com.nuecho.genesys.cli.commands.agent.status

import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.commons.protocol.MessageHandler
import com.genesyslab.platform.reporting.protocol.statserver.AgentStatus
import com.genesyslab.platform.reporting.protocol.statserver.DnActions
import com.genesyslab.platform.reporting.protocol.statserver.events.EventInfo
import com.genesyslab.platform.reporting.protocol.statserver.events.EventStatisticInvalid
import com.genesyslab.platform.reporting.protocol.statserver.events.EventStatisticOpened
import com.genesyslab.platform.reporting.protocol.statserver.requests.RequestOpenStatisticEx
import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import com.nuecho.genesys.cli.commands.agent.mockAgentStatus
import com.nuecho.genesys.cli.services.StatService
import com.nuecho.genesys.cli.services.StatServiceException
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.matchers.startWith
import io.kotlintest.specs.StringSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.experimental.launch

private const val MISSING_REQUIRED_OPTIONS = "Missing required options [--stat-host=<statHost>, --stat-port=<statPort>, params[0]=employeeId]"
private const val USAGE_PREFIX = "Usage: status [-?] --stat-host=<statHost> --stat-port=<statPort> employeeId"
private const val TEST_TIMEOUT = 1L

class StatusCommandTest : StringSpec() {
    init {
        "executing Status with no arguments should print usage" {
            val output = execute("agent", "status")
            output should startWith(MISSING_REQUIRED_OPTIONS)
        }

        "executing Status with -h argument should print usage" {
            val output = execute("agent", "status", "-h")
            output should startWith(USAGE_PREFIX)
        }

        "executing getAgentStatus should return AgentStatus" {
            val agentId = "test"
            val agent = mockAgent(agentId)
            val agentStatus = DnActions.WaitForNextCall

            val statServiceMock = mockStatService(mockAgentStatus(agentId, status = agentStatus))

            val output = Status.getAgentStatus(statServiceMock, agent, TEST_TIMEOUT)

            output.agentId shouldBe agentId
            output.status shouldBe agentStatus.asInteger()
        }

        "executing getAgentStatus should throw if statistic fails" {
            val agentId = "test"
            val agent = mockAgent(agentId)

            val statServiceMock = mockStatService(mockAgentStatus(agentId))

            // Fail EventStatistic creation
            every { statServiceMock.request(ofType(RequestOpenStatisticEx::class)) } returns EventStatisticInvalid.create()

            shouldThrow<StatServiceException> {
                Status.getAgentStatus(statServiceMock, agent, TEST_TIMEOUT)
            }
        }

        "executing getAgentStatus should throw if AgentStatus is never received" {
            val agentId = "test"
            val agent = mockAgent(agentId)

            val statServiceMock = mockStatService(mockAgentStatus(agentId))

            // Do not send AgentStatus
            every { statServiceMock.request(ofType(RequestOpenStatisticEx::class)) } returns EventStatisticOpened.create()

            shouldThrow<StatServiceException> {
                Status.getAgentStatus(statServiceMock, agent, TEST_TIMEOUT)
            }
        }

        "executing getAgentStatus should return AgentStatus" {
            val agentId = "test"
            val agent = mockAgent(agentId)
            val agentStatus = DnActions.WaitForNextCall

            val statServiceMock = mockStatService(mockAgentStatus(agentId, status = agentStatus))

            val output = Status.getAgentStatus(statServiceMock, agent, TEST_TIMEOUT)

            output.agentId shouldBe agentId
            output.status shouldBe agentStatus.asInteger()
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
