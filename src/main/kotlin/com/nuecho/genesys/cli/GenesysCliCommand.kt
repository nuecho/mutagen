package com.nuecho.genesys.cli

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.nuecho.genesys.cli.preferences.Preferences
import picocli.CommandLine

abstract class GenesysCliCommand {
    @Suppress("unused")
    @CommandLine.Option(
        names = ["-?", "-h", "--help"],
        usageHelp = true,
        description = ["Shows this help message."]
    )
    private var usageRequested = false

    @CommandLine.Option(
        names = ["-s", "--stacktrace"],
        description = ["Print out the stacktrace for all exceptions."]
    )
    var printStackTrace = false

    @CommandLine.Option(
        names = ["-e", "--env"],
        description = ["Environment name used for the execution."]
    )
    private var environmentName = Preferences.DEFAULT_ENVIRONMENT

    internal fun connect(): IConfService {
        val environment = Preferences.loadEnvironment(environmentName)
        val configurationService = GenesysServices.createConfigurationService(environment, CfgAppType.CFGSCE)
        configurationService.protocol.open()
        return configurationService
    }
}
