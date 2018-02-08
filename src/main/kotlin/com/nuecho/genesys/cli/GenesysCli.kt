package com.nuecho.genesys.cli

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.commons.connection.ConnectionException
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.nuecho.genesys.cli.GenesysServices.createConfigurationService
import com.nuecho.genesys.cli.preferences.Preferences
import com.nuecho.genesys.cli.preferences.Preferences.DEFAULT_ENVIRONMENT
import picocli.CommandLine
import java.io.PrintStream
import java.net.URISyntaxException

@CommandLine.Command(
        name = "mutagen",
        description = ["Your Genesys Toolbox"],
        versionProvider = VersionProvider::class)
class GenesysCli(private val out: PrintStream) : GenesysCliCommand(), Runnable {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val out = System.err
            CommandLine.run(GenesysCli(out), out, *args)
        }
    }

    override fun run() {
        CommandLine.usage(this, out)
    }

    @Suppress("unused")
    @CommandLine.Option(names = ["-v", "--version"],
            versionHelp = true,
            description = ["print version info"])
    private var versionRequested = false

    @CommandLine.Option(names = ["-e", "--env"],
            description = ["environment name"])
    private var environmentName = DEFAULT_ENVIRONMENT

    @Throws(ConnectionException::class, URISyntaxException::class)
    internal fun connect(): IConfService {
        val environment = Preferences.loadEnvironment(environmentName)
        val configurationService = createConfigurationService(environment, CfgAppType.CFGSCE)

        try {
            configurationService.protocol.open()
        } catch (exception: Exception) {
            throw ConnectionException("Can't create IConfService", exception)
        }

        return configurationService
    }
}