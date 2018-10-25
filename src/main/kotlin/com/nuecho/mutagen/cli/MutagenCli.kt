/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nuecho.mutagen.cli

import com.nuecho.mutagen.cli.commands.MutagenCliCommand
import com.nuecho.mutagen.cli.commands.agent.Agent
import com.nuecho.mutagen.cli.commands.audio.Audio
import com.nuecho.mutagen.cli.commands.config.Config
import com.nuecho.mutagen.cli.commands.services.Services
import com.nuecho.mutagen.cli.preferences.Preferences
import com.nuecho.mutagen.cli.preferences.SecurePassword
import picocli.CommandLine
import picocli.CommandLine.DefaultExceptionHandler
import picocli.CommandLine.Help
import picocli.CommandLine.ParameterException
import picocli.CommandLine.RunLast
import java.io.File
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
 Your Genesys Toolbox. (https://github.com/nuecho/mutagen)
 Nu Echo Inc - Contact: mutagen@nuecho.com
"""

const val SYNOPSIS = "mutagen [OPTIONS] <command>"
const val FOOTER = "See 'mutagen <command> --help' to read about a specific command."
const val EXTRA_FOOTER = "Please specify a command."
const val PASSWORD_FROM_STDIN = "--password-from-stdin"

@CommandLine.Command(
    name = "mutagen",
    customSynopsis = ["$SYNOPSIS%n"],
    versionProvider = VersionProvider::class,
    footer = ["%n$FOOTER"],
    commandListHeading = "%nCommands:%n%n",
    subcommands = [Agent::class, Audio::class, Config::class, Services::class]
)
open class MutagenCli : MutagenCliCommand() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                Console.enableAnsiMode()
                System.exit(execute(MutagenCli(), *args))
            } finally {
                Console.disableAnsiMode()
            }
        }

        @Suppress("PrintStackTrace")
        fun execute(mutagenCli: MutagenCli, vararg args: String): Int {
            try {
                val exceptionHandler = CliExceptionHandler()
                val result = CommandLine(mutagenCli)
                    .parseWithHandlers(RunLast(), System.out, Help.Ansi.AUTO, exceptionHandler, *args)

                if (exceptionHandler.exceptionOccurred)
                    return 1

                return (result?.getOrNull(0) ?: 0) as Int
            } catch (exception: CommandLine.InitializationException) {
                exception.printStackTrace()
                return 1
            } catch (exception: CommandLine.ExecutionException) {
                val cause = exception.cause!!

                if (mutagenCli.printStackTrace) {
                    // The only acceptable place to print the stack trace
                    cause.printStackTrace()
                } else {
                    System.err.println(cause.message ?: cause::class.qualifiedName)
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
        names = ["-p", PASSWORD_FROM_STDIN],
        description = ["Read password from standard input."]
    )
    var readPasswordFromStdin = false

    @CommandLine.Option(
        names = ["--metrics"],
        description = ["Performance metrics output file."]
    )
    var metricsFile: File? = null

    @Suppress("unused")
    @CommandLine.Option(
        names = ["-v", "--version"],
        versionHelp = true,
        description = ["Show version info."]
    )
    private var versionRequested = false

    override fun execute(): Int {
        CommandLine.usage(MutagenCliWithBanner(), System.out)

        return 0
    }

    override fun getMutagenCli() = this

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
class MutagenCliWithBanner : MutagenCli()

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
