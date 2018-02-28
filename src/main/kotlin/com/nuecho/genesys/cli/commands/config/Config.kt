package com.nuecho.genesys.cli.commands.config

import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.commands.config.export.Export
import com.nuecho.genesys.cli.commands.config.import.Import
import picocli.CommandLine

@CommandLine.Command(
    name = "config",
    description = ["Genesys Config Server tool"],
    subcommands = [Export::class, Import::class]
)
class Config : GenesysCliCommand() {
    @CommandLine.ParentCommand
    private var genesysCli: GenesysCli? = null

    override fun execute() {
        CommandLine.usage(this, System.out)
    }

    override fun getGenesysCli() = genesysCli!!
}
