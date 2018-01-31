package com.nuecho.genesys.cli

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.commons.connection.ConnectionException
import picocli.CommandLine
import java.net.URISyntaxException

@CommandLine.Command(name = "compare", description = ["Compare Genesys configuration between two instances."], subcommands = [(CompareAgents::class)])
class Compare : BasicCommand() {
    @CommandLine.ParentCommand
    private var genesysCli: GenesysCli? = null

    @Throws(ConnectionException::class, URISyntaxException::class)
    internal fun connect(): IConfService {
        return genesysCli!!.connect()
    }
}
