package com.nuecho.genesys.cli.agent

import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.agent.logout.Logout
import com.nuecho.genesys.cli.agent.status.Status
import picocli.CommandLine

@CommandLine.Command(
    name = "agent",
    description = ["Genesys Agents tool"],
    subcommands = [Logout::class, Status::class]
)
class Agent : GenesysCliCommand() {
    @CommandLine.ParentCommand
    private var genesysCli: GenesysCli? = null

    override fun execute() {
        CommandLine.usage(this, System.out)
    }

    override fun getGenesysCli(): GenesysCli = genesysCli!!
}
