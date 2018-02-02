package com.nuecho.genesys.cli

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.commons.connection.ConnectionException
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.nuecho.genesys.cli.GenesysServices.createConfigurationService
import com.nuecho.genesys.cli.preferences.Preferences
import com.nuecho.genesys.cli.preferences.Preferences.loadEnvironments
import picocli.CommandLine
import java.io.PrintStream
import java.net.URISyntaxException

@CommandLine.Command(
    name = "gen",
    description = ["Genesys Command Line Interface"])
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

    @Throws(ConnectionException::class, URISyntaxException::class)
    internal fun connect(): IConfService {
        val environments = Preferences.loadEnvironments()
        if (environments.size != 1) throw IllegalStateException("Only one environment can be configured for the moment.")

        val environment = environments.values.iterator().next()
        val configurationService = createConfigurationService(environment, CfgAppType.CFGSCE)

        try {
            configurationService.protocol.open()
        } catch (exception: Exception) {
            throw ConnectionException("Can't create IConfService", exception)
        }

        return configurationService
    }
}