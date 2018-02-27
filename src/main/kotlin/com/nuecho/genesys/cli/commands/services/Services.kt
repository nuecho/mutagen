package com.nuecho.genesys.cli.commands.services

import com.fasterxml.jackson.core.JsonGenerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging.debug
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.core.defaultGenerator
import com.nuecho.genesys.cli.getDefaultEndpoint
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectType
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.toShortName
import picocli.CommandLine
import java.io.IOException
import java.net.Socket
import java.net.URI
import kotlin.reflect.full.createInstance

@CommandLine.Command(
    name = "services",
    description = ["Discover services."]
)
class Services : GenesysCliCommand() {

    @CommandLine.ParentCommand
    private var genesysCli: GenesysCli? = null

    @CommandLine.Option(
        names = ["--types"],
        split = ",",
        description = ["Comma separated list of server application types (TServer,StatServer,OrchestrationServer,...)."]
    )
    private var types: List<String>? = null
        get() {
            return field?.map { it -> it.toLowerCase() }
        }

    @CommandLine.Option(
        names = ["--show-status"],
        description = ["Show service status."]
    )
    private var showStatus: Boolean = false

    override fun execute() {
        info { "Discovering services" }
        val service = ConfService(getGenesysCli().loadEnvironment())
        service.open()
        val applications = service.retrieveMultipleObjects(
            CfgApplication::class.java,
            ConfigurationObjectType.APPLICATION.queryType.createInstance()
        ) ?: emptyList()

        writeServices(applications)
    }

    internal fun writeServices(
        applications: Collection<CfgApplication>,
        jsonGenerator: JsonGenerator = defaultGenerator()
    ) {

        info { "Filtering over ${applications.size} potential service candidate(s)." }

        jsonGenerator.use {
            jsonGenerator.writeStartArray()
            applications
                //.filter { application -> accept(application, types) }
                .forEach { application ->
                    debug { "processing ${application.type}" }
                    if (accept(application, types)) {
                        val service = toService(application, showStatus)
                        jsonGenerator.writeObject(service)
                    }
                }
            jsonGenerator.writeEndArray()
        }
    }

    override fun getGenesysCli() = genesysCli!!

    companion object {

        internal fun accept(application: CfgApplication, types: List<String>? = null): Boolean {

            debug { "${application.type} ${application.getDefaultEndpoint()}" }
            if (!application.isServer.equals(CfgFlag.CFGTrue)) {
                debug { "Not a server" }
                return false
            }

            if (types != null && !types.contains(toShortName(application))) {
                debug { "Not part of filter" }
                return false
            }

            if (application.getDefaultEndpoint() == null) {
                debug { "No default endpoint" }
                return false
            }
            debug { "Accepting ${application.type}" }
            return true
        }

        internal fun toShortName(application: CfgApplication) = application.type.toShortName()

        internal fun toService(application: CfgApplication, showStatus: Boolean = false) =
            Service(
                name = application.name,
                type = toShortName(application),
                version = application.version.toString(),
                endpoint = application.getDefaultEndpoint()!!.uri,
                isPrimary = application.isPrimary == CfgFlag.CFGTrue,
                status = status(application.getDefaultEndpoint()!!, showStatus)
            )

        internal fun status(endpoint: Endpoint, showStatus: Boolean = false): Status {
            if (!showStatus) return Status.UNAVAILABLE

            info { "Trying TCP connection to ${endpoint.host}:${endpoint.port}." }

            return try {
                Socket(endpoint.host, endpoint.port)
                Status.UP
            } catch (e: IOException) {
                Status.DOWN
            }
        }
    }
}

data class Service(
    val name: String, val type: String, val version: String,
    val endpoint: URI, val isPrimary: Boolean = true, val status: Status = Status.UNAVAILABLE
)

enum class Status { UNAVAILABLE, DOWN, UP }
