package com.nuecho.genesys.cli

import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.Service
import com.nuecho.genesys.cli.services.withService
import picocli.CommandLine
import java.util.concurrent.Callable

abstract class GenesysCliCommand : Callable<Int> {
    @Suppress("unused")
    @CommandLine.Option(
        names = ["-?", "-h", "--help"],
        usageHelp = true,
        description = ["Show this help message."]
    )
    private var usageRequested = false

    @Suppress("UNCHECKED_CAST")
    internal fun <T> withEnvironmentConfService(function: (service: ConfService) -> T): T =
        withService(ConfService(getGenesysCli().loadEnvironment()), function as (service: Service) -> T)

    abstract fun execute(): Int

    abstract fun getGenesysCli(): GenesysCli

    override fun call(): Int {
        val genesysCli = getGenesysCli()

        if (genesysCli.debug) {
            Logging.setToDebug()
        } else if (genesysCli.info) {
            Logging.setToInfo()
        }

        return execute()
    }
}
