package com.nuecho.genesys.cli

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
