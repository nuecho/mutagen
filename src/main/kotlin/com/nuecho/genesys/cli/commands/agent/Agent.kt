package com.nuecho.genesys.cli.commands.agent

import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.commands.GenesysCliCommand
import com.nuecho.genesys.cli.commands.agent.logout.LogoutCommand
import com.nuecho.genesys.cli.commands.agent.status.StatusCommand
import picocli.CommandLine

@CommandLine.Command(
    name = "agent",
    description = ["Genesys Agents tool"],
    subcommands = [LogoutCommand::class, StatusCommand::class]
)
class Agent : GenesysCliCommand() {
    @CommandLine.ParentCommand
    private var genesysCli: GenesysCli? = null

    override fun execute(): Int {
        CommandLine.usage(this, System.out)

        return 0
    }

    override fun getGenesysCli(): GenesysCli = genesysCli!!
}
