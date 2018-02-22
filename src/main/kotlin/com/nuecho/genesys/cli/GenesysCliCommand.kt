package com.nuecho.genesys.cli

import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.Service
import com.nuecho.genesys.cli.services.withService
import picocli.CommandLine

abstract class GenesysCliCommand : Runnable {
    @Suppress("unused")
    @CommandLine.Option(
        names = ["-?", "-h", "--help"],
        usageHelp = true,
        description = ["Show this help message."]
    )
    private var usageRequested = false

    @Suppress("UNCHECKED_CAST")
    internal fun withEnvironmentConfService(function: (service: ConfService) -> Any?) =
        withService(ConfService(getGenesysCli().loadEnvironment()), function as (service: Service) -> Any?)

    abstract fun execute()

    abstract fun getGenesysCli(): GenesysCli

    override fun run() {
        val genesysCli = getGenesysCli()

        if (genesysCli.debug) {
            Logging.setToDebug()
        } else if (genesysCli.info) {
            Logging.setToInfo()
        }

        execute()
    }
}
