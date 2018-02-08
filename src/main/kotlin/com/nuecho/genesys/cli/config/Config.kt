package com.nuecho.genesys.cli.config

import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.config.export.Export
import picocli.CommandLine

@CommandLine.Command(
    name = "config",
    description = ["Genesys Config Server tool"],
    subcommands = [Export::class]
)
class Config : GenesysCliCommand(), Runnable {
    override fun run() {
        CommandLine.usage(this, System.out)
    }
}
