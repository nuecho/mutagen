package com.nuecho.genesys.cli.commands.agent.logout

import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.reporting.protocol.statserver.AgentStatus
import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import com.nuecho.genesys.cli.commands.agent.mockAgentStatus
import com.nuecho.genesys.cli.commands.agent.status.Status
import com.nuecho.genesys.cli.getDefaultEndpoint
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.TService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.objectMockk
import io.mockk.staticMockk
import io.mockk.use
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val MISSING_REQUIRED_OPTIONS = "Missing required options [--stat-host=<statHost>, --stat-port=<statPort>, params[ 0]=employeeId]"
private const val USAGE_PREFIX = "Usage: logout [-?] --stat-host=<statHost> --stat-port=<statPort> employeeId"

class LogoutCommandTest {
    @Test
    fun `executing Logout with no arguments should print an error message`() {
        val output = execute("agent", "logout")
        assertTrue(output.startsWith(MISSING_REQUIRED_OPTIONS))
    }

    @Test
    fun `executing Logout with -h argument should print usage`() {
        val output = execute("agent", "logout", "-h")
        assertTrue(output.startsWith(USAGE_PREFIX))
    }

    @Test
    fun `executing logoutAgent with an employeeId should call getAgentStatus`() {
        val agentId = "test"
        val agentStatus = mockk<AgentStatus>(relaxed = true)

        objectMockk(Status).use {
            every { Status.getAgentStatus(any(), any(), agentId) } returns agentStatus

            Logout.logoutAgent(mockk(), mockk(), agentId)

            verify { Status.getAgentStatus(any(), any(), agentId) }
        }
    }

    @Test
    fun `executing logoutAgent should call logout on dns`() {
        val dns = listOf("1234")
        val agentStatus = mockAgentStatus()

        val switchName = "testSwitch"
        val switch = mockCfgSwitch(switchName)

        val service = mockConfService()
        every { service.retrieveObject(CfgSwitch::class.java, any()) } returns switch

        val tService = mockTService()

        staticMockk("com.nuecho.genesys.cli.commands.agent.logout.LogoutCommandKt").use {
            every { switch.getTService() } returns tService
            every { agentStatus.toSwitchIdDnMap() } returns mapOf(switchName to dns)

            Logout.logoutAgent(service, agentStatus)

            verify { tService.logoutAddresses(dns) }
        }
    }

    @Test
    fun `executing getTService on a CfgSwitch should return a TService`() {
        val endpoint = Endpoint("test", 1234)

        val switch = mockk<CfgSwitch>()
        val tServer = mockk<CfgApplication>()
        every { switch.tServer } returns tServer

        staticMockk("com.nuecho.genesys.cli.GenesysExtensionsKt").use {
            every { tServer.getDefaultEndpoint() } returns endpoint

            val tService = switch.getTService()

            assertEquals(tService!!.endpoint, endpoint)
        }
    }
}

private fun mockTService(): TService {
    val tService = mockk<TService>()

    every { tService.endpoint } returns Endpoint("testEndpoint", 1234)
    every { tService.open() } just Runs
    every { tService.close() } just Runs
    every { tService.logoutAddress(any()) } just Runs
    every { tService.logoutAddresses(any()) } just Runs

    return tService
}
