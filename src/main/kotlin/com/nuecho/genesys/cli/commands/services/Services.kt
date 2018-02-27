package com.nuecho.genesys.cli.commands.services

import com.fasterxml.jackson.core.JsonGenerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.core.defaultGenerator
import com.nuecho.genesys.cli.getDefaultEndpoint
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectType
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.toShortName
import picocli.CommandLine
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
        configurationObjects: Collection<CfgApplication>,
        jsonGenerator: JsonGenerator = defaultGenerator()
    ) {

        info { "Filtering over ${configurationObjects.size} potential service candidate(s)." }

        jsonGenerator.use {
            jsonGenerator.writeStartArray()
            configurationObjects
                .filter { it -> accept(it, types) }
                .forEach {
                    jsonGenerator.writeObject(toService(it))
                }
            jsonGenerator.writeEndArray()
        }
    }

    override fun getGenesysCli() = genesysCli!!

    companion object {

        internal fun accept(application: CfgApplication, types: List<String>? = null): Boolean {
            if (!application.isServer.equals(CfgFlag.CFGTrue)) return false

            if (application.getDefaultEndpoint() == null) return false

            if (types != null && !types.contains(toShortName(application))) return false

            return true
        }

        internal fun toShortName(application: CfgApplication) = application.type.toShortName()

        fun toService(application: CfgApplication) =
            Service(
                name = application.name,
                type = toShortName(application),
                version = application.version.toString(),
                endpoint = application.getDefaultEndpoint()!!.uri,
                isPrimary = application.isPrimary == CfgFlag.CFGTrue
            )
    }
}

data class Service(
    val name: String, val type: String, val version: String,
    val endpoint: URI, val isPrimary: Boolean = true
)
