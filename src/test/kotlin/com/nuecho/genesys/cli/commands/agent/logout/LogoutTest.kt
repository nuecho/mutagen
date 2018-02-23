package com.nuecho.genesys.cli.commands.agent.logout

import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.reporting.protocol.statserver.AgentStatus
import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import com.nuecho.genesys.cli.commands.agent.mockAgentStatus
import com.nuecho.genesys.cli.services.TService
import io.kotlintest.matchers.should
import io.kotlintest.matchers.startWith
import io.kotlintest.specs.StringSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk

private const val MISSING_USERNAME = "Missing required parameter: username"
private const val USAGE_PREFIX = "Usage: logout [-?] [--stat-host=<statHost>] [--stat-port=<statPort>] username"

class LogoutTest : StringSpec() {
    init {
        "executing Logout with no arguments should print an error message" {
            val output = execute("agent", "logout")
            output should startWith(MISSING_USERNAME)
        }

        "executing Logout with -h argument should print usage" {
            val output = execute("agent", "logout", "-h")
            output should startWith(USAGE_PREFIX)
        }

        "executing Logout should work" {
            val agentId = "test"

            val agentStatusProvider: (String) -> AgentStatus = { mockAgentStatus(agentId) }
            val tServiceProvider: (String) -> TService = { mockTService() }

            Logout().logoutAgent(agentId, agentStatusProvider, tServiceProvider)
        }
    }
}

private fun mockTService(): TService {
    val tService = mockk<TService>()
    every { tService.endpoint } returns Endpoint("testEndpoint", 1234)
    every { tService.open() } just Runs
    every { tService.close() } just Runs
    every { tService.logoutAdress(any()) } just Runs
    return tService
}
