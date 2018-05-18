package com.nuecho.genesys.cli.commands.agent.logout

import com.genesyslab.platform.applicationblocks.com.ConfigServerException
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.reporting.protocol.statserver.AgentStatus
import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.commands.agent.Agent
import com.nuecho.genesys.cli.commands.agent.status.Status
import com.nuecho.genesys.cli.getDefaultEndpoint
import com.nuecho.genesys.cli.isLoggedOut
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.StatService
import com.nuecho.genesys.cli.services.TService
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.services.withService
import com.nuecho.genesys.cli.toConsoleString
import com.nuecho.genesys.cli.toList
import picocli.CommandLine

@CommandLine.Command(
    name = "logout",
    description = ["Logout Agent"]
)
class LogoutCommand : GenesysCliCommand() {
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

    override fun execute() {
        withEnvironmentConfService {
            Logout.logoutAgent(it, StatService(Endpoint(statHost!!, statPort!!)), employeeId!!)
        }
    }

    override fun getGenesysCli(): GenesysCli = agent!!.getGenesysCli()
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
                val switch = confService.retrieveObject(SwitchReference(name))
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
