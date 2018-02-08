package com.nuecho.genesys.cli

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.commons.connection.ConnectionException
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.nuecho.genesys.cli.preferences.Preferences
import picocli.CommandLine

abstract class GenesysCliCommand {
    @Suppress("unused")
    @CommandLine.Option(names = ["-h", "--help"],
        usageHelp = true,
        description = ["display a help message"])
    private var usageRequested = false

    @CommandLine.Option(
        names = ["-e", "--env"],
        description = ["environment name"])
    private var environmentName = Preferences.DEFAULT_ENVIRONMENT

    internal fun connect(): IConfService {
        val environment = Preferences.loadEnvironment(environmentName)
        val configurationService = GenesysServices.createConfigurationService(environment, CfgAppType.CFGSCE)

        try {
            configurationService.protocol.open()
        } catch (exception: Exception) {
            throw ConnectionException("Can't create IConfService", exception)
        }

        return configurationService
    }
}
