package com.nuecho.genesys.cli

import com.nuecho.genesys.cli.commands.GenesysCliCommand
import com.nuecho.genesys.cli.commands.agent.Agent
import com.nuecho.genesys.cli.commands.audio.Audio
import com.nuecho.genesys.cli.commands.config.Config
import com.nuecho.genesys.cli.commands.password.SetPasswordCommand
import com.nuecho.genesys.cli.commands.services.Services
import com.nuecho.genesys.cli.preferences.Preferences
import com.nuecho.genesys.cli.preferences.SecurePassword
import picocli.CommandLine
import picocli.CommandLine.DefaultExceptionHandler
import picocli.CommandLine.Help
import picocli.CommandLine.ParameterException
import picocli.CommandLine.RunLast
import java.io.PrintStream

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
            try {
                Console.enableAnsiMode()
                System.exit(execute(GenesysCli(), *args))
            } finally {
                Console.disableAnsiMode()
            }
        }

        @Suppress("PrintStackTrace")
        fun execute(genesysCli: GenesysCli, vararg args: String): Int {
            try {
                val exceptionHandler = CliExceptionHandler()
                val result = CommandLine(genesysCli)
                    .parseWithHandlers(RunLast(), System.out, Help.Ansi.AUTO, exceptionHandler, *args)

                if (exceptionHandler.exceptionOccurred)
                    return 1

                return (result?.getOrNull(0) ?: 0) as Int
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

    @CommandLine.Option(
        names = ["-p", "--read-password-from-stdin"],
        description = ["Read password from standard input."]
    )
    var readPasswordFromStdin = false

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

    internal fun loadEnvironment(password: SecurePassword?) = Preferences.loadEnvironment(
        environmentName = environmentName,
        password = password
    )
}

@CommandLine.Command(
    headerHeading = "@|fg(green) $BANNER|@%n",
    footer = [
        "%n$FOOTER",
        "%n@|fg(red) $EXTRA_FOOTER|@"
    ]
)
class GenesysCliWithBanner : GenesysCli()

private class CliExceptionHandler : DefaultExceptionHandler() {
    var exceptionOccurred = false

    override fun handleException(
        exception: ParameterException?,
        out: PrintStream?,
        ansi: Help.Ansi?,
        vararg args: String?
    ): MutableList<Any> {
        exceptionOccurred = true

        return super.handleException(exception, out, ansi, *args)
    }
}
