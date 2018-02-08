package com.nuecho.genesys.cli

import com.nuecho.genesys.cli.config.Config
import picocli.CommandLine

@CommandLine.Command(
    name = "mutagen",
    description = ["Your Genesys Toolbox"],
    versionProvider = VersionProvider::class,
    subcommands = [Config::class]
)
class GenesysCli : GenesysCliCommand(), Runnable {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine.run(GenesysCli(), System.out, *args)
        }
    }

    override fun run() {
        CommandLine.usage(this, System.out)
    }

    @Suppress("unused")
    @CommandLine.Option(
        names = ["-v", "--version"],
        versionHelp = true,
        description = ["print version info"]
    )
    private var versionRequested = false
}
