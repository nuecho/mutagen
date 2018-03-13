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
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import kotlin.reflect.full.createInstance

const val DEFAULT_SOCKET_TIMEOUT = 200

@CommandLine.Command(
    name = "services",
    description = ["Discover services"],
    showDefaultValues = true
)
class Services : GenesysCliCommand() {

    @CommandLine.ParentCommand
    private var genesysCli: GenesysCli? = null

    @CommandLine.Option(
        names = ["--types"],
        split = ",",
        description = ["Comma separated list of server application types to filter on " +
                "(TServer,StatServer,OrchestrationServer,...)."]
    )
    private var types: List<String>? = null
        get() = field?.map { it.toLowerCase() }

    @CommandLine.Option(
        names = ["--show-status"],
        description = ["Show service status."]
    )
    private var showStatus: Boolean = false

    @CommandLine.Option(
        names = ["--timeout"],
        description = ["Set socket connection timeout (ms)."]
    )
    private var socketTimeout: Int = DEFAULT_SOCKET_TIMEOUT

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
                .filter { application -> accept(application, types) }
                .map { application -> toServiceDefinition(application, showStatus, socketTimeout) }
                .forEach { service ->
                    jsonGenerator.writeObject(service)
                }
            jsonGenerator.writeEndArray()
        }
    }

    override fun getGenesysCli() = genesysCli!!

    companion object {

        internal fun accept(application: CfgApplication, types: List<String>? = null): Boolean {

            if (application.isServer != CfgFlag.CFGTrue) return false

            if (application.getDefaultEndpoint() == null) return false

            if (types == null) return true // no filter applied

            return types.contains(application.type.toShortName())
        }

        internal fun toServiceDefinition(
            application: CfgApplication,
            showStatus: Boolean = false, socketTimeout: Int = DEFAULT_SOCKET_TIMEOUT
        ) =
            ServiceDefinition(
                name = application.name,
                type = application.type.toShortName(),
                version = application.version.toString(),
                endpoint = application.getDefaultEndpoint()?.uri,
                isPrimary = application.isPrimary == CfgFlag.CFGTrue,
                status = status(application.getDefaultEndpoint(), showStatus, socketTimeout)
            )

        internal fun status(
            endpoint: Endpoint?,
            showStatus: Boolean = false, socketTimeout: Int = DEFAULT_SOCKET_TIMEOUT
        ): Status? {
            if (endpoint == null) return null
            if (!showStatus) return null

            info { "Trying TCP connection to ${endpoint.host}:${endpoint.port}." }
            return try {
                Socket().connect(InetSocketAddress(endpoint.host, endpoint.port), socketTimeout)
                Status.UP
            } catch (e: Throwable) {
                debug { e.message }
                Status.DOWN
            }
        }
    }
}

data class ServiceDefinition(
    val name: String, val type: String, val version: String,
    val endpoint: URI?, val isPrimary: Boolean = true, val status: Status? = null
)

enum class Status { DOWN, UP }
