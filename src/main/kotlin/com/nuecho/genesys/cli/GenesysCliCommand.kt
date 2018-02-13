package com.nuecho.genesys.cli

import mu.KotlinLogging
import picocli.CommandLine

private val logger = KotlinLogging.logger {}

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

    // Logging
    internal fun info(message: () -> Any?) {
        logger.info(message)
    }

    internal fun debug(message: () -> Any?) {
        logger.debug(message)
    }
}
