package com.nuecho.genesys.cli

import picocli.CommandLine

abstract class GenesysCliCommand : Runnable {
    @Suppress("unused")
    @CommandLine.Option(
        names = ["-?", "-h", "--help"],
        usageHelp = true,
        description = ["Shows this help message."]
    )
    private var usageRequested = false

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
