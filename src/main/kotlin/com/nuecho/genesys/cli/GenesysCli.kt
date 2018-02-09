package com.nuecho.genesys.cli

import com.nuecho.genesys.cli.config.Config
import picocli.CommandLine

@CommandLine.Command(
    name = "mutagen",
    description = ["Your Genesys Toolbox"],
    versionProvider = VersionProvider::class,
    subcommands = [Config::class]
)
open class GenesysCli : GenesysCliCommand(), Runnable {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.exit(execute(GenesysCli(), *args))
        }

        fun execute(genesysCli: GenesysCli, vararg args: String): Int {
            try {
                CommandLine.run(genesysCli, System.out, *args)
            } catch (exception: CommandLine.InitializationException) {
                exception.printStackTrace()
                return 1
            } catch (exception: CommandLine.ExecutionException) {
                val cause = exception.cause!!
                val command: GenesysCliCommand = exception.commandLine.getCommand()

                if (command.printStackTrace) {
                    cause.printStackTrace()
                } else {
                    System.err.println(cause.message)
                }

                return 1
            }

            return 0
        }
    }

    override fun run() {
        CommandLine.usage(this, System.out)
    }

    @Suppress("unused")
    @CommandLine.Option(
        names = ["-v", "--version"],
        versionHelp = true,
        description = ["Shows version info."]
    )
    private var versionRequested = false
}
