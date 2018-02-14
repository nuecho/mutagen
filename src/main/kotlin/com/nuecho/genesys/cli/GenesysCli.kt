package com.nuecho.genesys.cli

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.nuecho.genesys.cli.config.Config
import com.nuecho.genesys.cli.preferences.Preferences
import picocli.CommandLine

const val BANNER = """
_   .-')                .-') _      ('-.                   ('-.       .-') _
( '.( OO )_             (  OO) )    ( OO ).-.             _(  OO)     ( OO ) )
 ,--.   ,--.),--. ,--.  /     '._   / . --. /  ,----.    (,------.,--./ ,--,'
 |   `.'   | |  | |  |  |'--...__)  | \-.  \  '  .-./-')  |  .---'|   \ |  |\
 |         | |  | | .-')'--.  .--'.-'-'  |  | |  |_( O- ) |  |    |    \|  | )
 |  |'.'|  | |  |_|( OO )  |  |    \| |_.'  | |  | .--, \(|  '--. |  .     |/
 |  |   |  | |  | | `-' /  |  |     |  .-.  |(|  | '. (_/ |  .--' |  |\    |
 |  |   |  |('  '-'(_.-'   |  |     |  | |  | |  '--'  |  |  `---.|  | \   |
 `--'   `--'  `-----'      `--'     `--' `--'  `------'   `------'`--'  `--'
"""

@CommandLine.Command(
    name = "mutagen",
    description = ["Your Genesys Toolbox. (https://sites.google.com/m.nuecho.com/hub/mutagen)"],
    versionProvider = VersionProvider::class,
    subcommands = [Config::class]
)
open class GenesysCli : GenesysCliCommand() {
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

                if (genesysCli.printStackTrace) {
                    cause.printStackTrace()
                } else {
                    System.err.println(cause.message)
                }

                return 1
            }

            return 0
        }
    }

    @CommandLine.Option(
        names = ["-s", "--stacktrace"],
        description = ["Print out the stacktrace for all exceptions."]
    )
    var printStackTrace = false

    @CommandLine.Option(
        names = ["-i", "--info"],
        description = ["Set log level to info."]
    )
    var info = false

    @CommandLine.Option(
        names = ["-d", "--debug"],
        description = ["Set log level to debug."]
    )
    var debug = false

    @CommandLine.Option(
        names = ["-e", "--env"],
        description = ["Environment name used for the execution."]
    )
    var environmentName = Preferences.DEFAULT_ENVIRONMENT

    @Suppress("unused")
    @CommandLine.Option(
        names = ["-v", "--version"],
        versionHelp = true,
        description = ["Shows version info."]
    )
    private var versionRequested = false

    override fun execute() {
        CommandLine.usage(GenesysCliWithBanner(), System.out)
    }

    override fun getGenesysCli(): GenesysCli {
        return this
    }

    internal fun connect(): IConfService {
        val environment = Preferences.loadEnvironment(environmentName)
        val configurationService = GenesysServices.createConfigurationService(environment, CfgAppType.CFGSCE)
        configurationService.protocol.open()
        return configurationService
    }
}

@CommandLine.Command(headerHeading = "@|fg(green) $BANNER |@\n")
class GenesysCliWithBanner : GenesysCli()
