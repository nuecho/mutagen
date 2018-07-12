package com.nuecho.genesys.cli.commands

import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.Logging
import com.nuecho.genesys.cli.preferences.SecurePassword
import picocli.CommandLine
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Callable

abstract class GenesysCliCommand : Callable<Int> {
    @Suppress("unused")
    @CommandLine.Option(
        names = ["-?", "-h", "--help"],
        usageHelp = true,
        description = ["Show this help message."]
    )
    private var usageRequested = false

    internal var password: SecurePassword? = null

    abstract fun execute(): Int

    abstract fun getGenesysCli(): GenesysCli

    override fun call(): Int {
        val genesysCli = getGenesysCli()

        if (genesysCli.debug) {
            Logging.setToDebug()
        } else if (genesysCli.info) {
            Logging.setToInfo()
        }

        if (getGenesysCli().readPasswordFromStdin) {
            val passwordReader = BufferedReader(InputStreamReader(System.`in`))
            password = SecurePassword(passwordReader.readLine().toCharArray())
        }

        return execute()
    }
}
