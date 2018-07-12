package com.nuecho.genesys.cli.commands.config

import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.commands.GenesysCliCommand
import com.nuecho.genesys.cli.commands.config.export.ExportCommand
import com.nuecho.genesys.cli.commands.config.import.ImportCommand
import picocli.CommandLine

@CommandLine.Command(
    name = "config",
    description = ["Genesys Config Server tool"],
    subcommands = [ExportCommand::class, ImportCommand::class]
)
class Config : GenesysCliCommand() {
    @CommandLine.ParentCommand
    private var genesysCli: GenesysCli? = null

    override fun execute(): Int {
        CommandLine.usage(this, System.out)

        return 0
    }

    override fun getGenesysCli() = genesysCli!!
}
