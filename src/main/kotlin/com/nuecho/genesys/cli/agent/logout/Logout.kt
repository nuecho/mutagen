package com.nuecho.genesys.cli.agent.logout

import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.queries.CfgSwitchQuery
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.reporting.protocol.statserver.AgentStatus
import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.agent.Agent
import com.nuecho.genesys.cli.agent.StatService
import com.nuecho.genesys.cli.agent.TService
import com.nuecho.genesys.cli.agent.status.Status
import com.nuecho.genesys.cli.config.ConfigurationService
import com.nuecho.genesys.cli.config.RemoteConfigurationService
import com.nuecho.genesys.cli.getDefaultEndpoint
import com.nuecho.genesys.cli.isLoggedOut
import com.nuecho.genesys.cli.toConsoleString
import com.nuecho.genesys.cli.toList
import picocli.CommandLine

@CommandLine.Command(
    name = "logout",
    description = ["Logout Agent"]
)
class Logout : GenesysCliCommand() {
    @CommandLine.ParentCommand
    private var agent: Agent? = null

    @CommandLine.Option(
        arity = "1",
        names = ["--stat-host"],
        description = ["Stat server hostname."]
    )
    private var statHost: String? = null

    @CommandLine.Option(
        arity = "1",
        names = ["--stat-port"],
        description = ["Stat server port."]
    )
    private var statPort: Int? = null

    @CommandLine.Parameters(
        arity = "1",
        index = " 0",
        paramLabel = "username",
        description = ["Username of the agent to logout."]
    )
    private var username: String? = null

    override fun execute() {
        val statService = StatService(Endpoint(statHost!!, statPort!!))
        val configService = RemoteConfigurationService(getGenesysCli().loadEnvironment())

        configService.connect()
        try {
            logoutAgent(username!!, agentStatusProvider(statService), tServiceProvider(configService))
        } finally {
            configService.disconnect()
        }
    }

    override fun getGenesysCli(): GenesysCli = agent!!.getGenesysCli()

    fun logoutAgent(
        username: String,
        getAgentStatus: (username: String) -> AgentStatus,
        getTService: (switchId: String) -> TService
    ) {
        val agentStatus = getAgentStatus(username)
        info { agentStatus.toConsoleString() }
        if (agentStatus.isLoggedOut()) return

        val switchIdDnPairs = agentStatus.place.dnStatuses.toList().map { Pair(it.switchId, it.dnId) }
        val tServerEndpointDnPairs = switchIdDnPairs.map { (switchId, dn) -> Pair(getTService(switchId), dn) }

        tServerEndpointDnPairs.forEach { (tService, dn) ->
            tService.open()
            try {
                tService.logoutAdress(dn)
                info { "Agent ($username) logged out from TServer (${tService.endpoint})." }
            } finally {
                tService.close()
            }
        }
    }

    private fun agentStatusProvider(statService: StatService): (username: String) -> AgentStatus =
        { Status().getAgentStatus(it, statService) }

    private fun tServiceProvider(configService: ConfigurationService): (switchId: String) -> TService =
        { TService(configService.getSwitchCfg(it).tServer.getDefaultEndpoint()) }

    private fun ConfigurationService.getSwitchCfg(name: String): CfgSwitch =
        this.retrieveObject(CfgSwitch::class.java, CfgSwitchQuery(name))
}
