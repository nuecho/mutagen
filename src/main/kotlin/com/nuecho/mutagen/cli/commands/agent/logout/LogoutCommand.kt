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

package com.nuecho.mutagen.cli.commands.agent.logout

import com.genesyslab.platform.applicationblocks.com.ConfigServerException
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.queries.CfgSwitchQuery
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.reporting.protocol.statserver.AgentStatus
import com.nuecho.mutagen.cli.Logging
import com.nuecho.mutagen.cli.Logging.info
import com.nuecho.mutagen.cli.MutagenCli
import com.nuecho.mutagen.cli.commands.ConfigServerCommand
import com.nuecho.mutagen.cli.commands.agent.Agent
import com.nuecho.mutagen.cli.commands.agent.status.Status
import com.nuecho.mutagen.cli.getDefaultEndpoint
import com.nuecho.mutagen.cli.isLoggedOut
import com.nuecho.mutagen.cli.preferences.environment.Environment
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.services.Services.withService
import com.nuecho.mutagen.cli.services.StatService
import com.nuecho.mutagen.cli.services.TService
import com.nuecho.mutagen.cli.toConsoleString
import com.nuecho.mutagen.cli.toList
import picocli.CommandLine

@CommandLine.Command(
    name = "logout",
    description = ["Logout Agent"]
)
class LogoutCommand : ConfigServerCommand() {
    @CommandLine.ParentCommand
    private var agent: Agent? = null

    @CommandLine.Option(
        arity = "1",
        names = ["--stat-host"],
        description = ["Stat server hostname."],
        required = true
    )
    private var statHost: String? = null

    @CommandLine.Option(
        arity = "1",
        names = ["--stat-port"],
        description = ["Stat server port."],
        required = true
    )
    private var statPort: Int? = null

    @CommandLine.Parameters(
        arity = "1",
        index = " 0",
        paramLabel = "employeeId",
        description = ["EmployeeId of the agent to logout."]
    )
    private var employeeId: String? = null

    override fun execute(): Int {
        withEnvironmentConfService { service: ConfService, _: Environment ->
            Logout.logoutAgent(service, StatService(Endpoint(statHost!!, statPort!!)), employeeId!!)
        }

        return 0
    }

    override fun getMutagenCli(): MutagenCli = agent!!.getMutagenCli()
}

object Logout {
    fun logoutAgent(confService: ConfService, statService: StatService, employeeId: String) =
        logoutAgent(confService, Status.getAgentStatus(confService, statService, employeeId))

    fun logoutAgent(confService: ConfService, agentStatus: AgentStatus) {
        Logging.info { agentStatus.toConsoleString() }
        if (agentStatus.isLoggedOut()) return

        agentStatus
            .toSwitchIdDnMap()
            .mapKeys {
                val name = it.key
                val switch = confService.retrieveObject(CfgSwitch::class.java, CfgSwitchQuery(name))
                        ?: throw ConfigServerException("Error while retrieving CfgSwitch ($name)")
                switch.getTService()
            }
            .filterKeys { it != null }
            .mapKeys { it.key as TService }
            .forEach { (tService, dns) ->
                withService(tService) { tService.logoutAddresses(dns) }
                info { "Agent (${agentStatus.agentId}) logged out from TServer (${tService.endpoint})." }
            }
    }
}

internal fun CfgSwitch.getTService(): TService? {
    val endpoint = tServer?.getDefaultEndpoint() ?: return null
    return TService(endpoint)
}

internal fun AgentStatus.toSwitchIdDnMap(): Map<String, List<String>> {
    val dnStatuses = place?.dnStatuses?.toList().orEmpty()
    // TODO SwitchId == null means e-service we gonna have to handle them differently
    return dnStatuses.filterNot { it.switchId == null }.groupBy({ it.switchId }, { it.dnId })
}
