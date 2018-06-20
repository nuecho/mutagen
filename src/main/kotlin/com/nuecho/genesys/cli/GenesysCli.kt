package com.nuecho.genesys.cli

import com.nuecho.genesys.cli.commands.agent.Agent
import com.nuecho.genesys.cli.commands.audio.Audio
import com.nuecho.genesys.cli.commands.config.Config
import com.nuecho.genesys.cli.commands.password.SetPasswordCommand
import com.nuecho.genesys.cli.commands.services.Services
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
 Your Genesys Toolbox. (https://sites.google.com/m.nuecho.com/hub/mutagen)
"""

const val SYNOPSIS = "mutagen [OPTIONS] <command>"
const val FOOTER = "See 'mutagen <command> --help' to read about a specific command."
const val EXTRA_FOOTER = "Please specify a command."

@CommandLine.Command(
    name = "mutagen",
    customSynopsis = ["$SYNOPSIS%n"],
    versionProvider = VersionProvider::class,
    footer = ["%n$FOOTER"],
    commandListHeading = "%nCommands:%n%n",
    subcommands = [Agent::class, Audio::class, Config::class, SetPasswordCommand::class, Services::class]
)
open class GenesysCli : GenesysCliCommand() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.exit(execute(GenesysCli(), *args))
        }

        @Suppress("PrintStackTrace")
        fun execute(genesysCli: GenesysCli, vararg args: String): Int {
            try {
                return CommandLine.call(genesysCli, System.out, *args) ?: 0
            } catch (exception: CommandLine.InitializationException) {
                exception.printStackTrace()
                return 1
            } catch (exception: CommandLine.ExecutionException) {
                val cause = exception.cause!!

                if (genesysCli.printStackTrace) {
                    // The only acceptable place to print the stack trace
                    cause.printStackTrace()
                } else {
                    System.err.println(cause.message)
                }

                return 1
            }
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
        description = ["Show version info."]
    )
    private var versionRequested = false

    override fun execute(): Int {
        CommandLine.usage(GenesysCliWithBanner(), System.out)

        return 0
    }

    override fun getGenesysCli() = this

    internal fun loadEnvironment() = Preferences.loadEnvironment(environmentName)
}

@CommandLine.Command(
    headerHeading = "@|fg(green) $BANNER|@%n",
    footer = [
        "%n$FOOTER",
        "%n@|fg(red) $EXTRA_FOOTER|@"
    ]
)
class GenesysCliWithBanner : GenesysCli()
