package com.nuecho.genesys.cli

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.commons.connection.ConnectionException
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.nuecho.genesys.cli.GenesysServices.createConfigurationService
import com.nuecho.genesys.cli.GenesysServices.createEndPoint
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import picocli.CommandLine
import java.net.URI
import java.net.URISyntaxException
import java.util.UUID

@CommandLine.Command(
    name = "gen",
    description = ["Genesys Command Line Interface"],
    version = [
        "@|yellow Versioned Command 1.0|@",
        "@|blue Build 12345|@",
        "@|red,bg(white) (c) 2017|@"
    ],
    subcommands = [(Compare::class)])
class GenesysCli : BasicCommand(), Runnable {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine.run(GenesysCli(), System.err, *args)
        }

        fun test(): Boolean {
            return true
        }
    }

    @CommandLine.Option(names = ["-v", "--version"],
        versionHelp = true,
        description = ["display version"])
    private var versionRequested = false

    @CommandLine.Option(
        names = ["-s", "--server"],
        paramLabel = "SERVER",
        description = ["the Genesys server to connect to"])
    private var server: String = ""

    @CommandLine.Option(
        names = ["-u", "--user"],
        paramLabel = "USER",
        description = ["the Genesys username for the connection"])
    private var username: String = ""

    @CommandLine.Option(
        names = ["-p", "--password"],
        paramLabel = "PASSWORD",
        description = ["the Genesys user password for the connection"])
    private var password: String = ""

    override fun run() {
        val rawConfiguration = ConfigFactory.load().root().render(ConfigRenderOptions.concise())
        val configuration = jacksonObjectMapper().readValue(rawConfiguration, Configuration::class.java)
        println(configuration)
    }

    @Throws(ConnectionException::class, URISyntaxException::class)
    internal fun connect(): IConfService {
        val endpoint = createEndPoint(
            URI("tcp://$server:${GenesysServices.DEFAULT_SERVER_PORT}/"),
            UUID.randomUUID().toString(),
            GenesysServices.DEFAULT_CLIENT_TIMEOUT,
            GenesysServices.DEFAULT_SERVER_TIMEOUT,
            false)

        return createConfigurationService(
            username,
            password,
            GenesysServices.DEFAULT_APPLICATION_NAME,
            CfgAppType.CFGSCE,
            endpoint)
    }
}