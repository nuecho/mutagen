package com.nuecho.genesys.cli.commands.config.export

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging.debug
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.commands.config.Config
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectType
import com.nuecho.genesys.cli.services.ConfService
import picocli.CommandLine
import kotlin.reflect.full.createInstance

@CommandLine.Command(
    name = "export",
    description = ["Genesys Config Server export tool"]
)
class Export : GenesysCliCommand() {
    @CommandLine.ParentCommand
    private var config: Config? = null

    override fun execute() {
        exportConfiguration(
            JsonExportProcessor(System.out),
            ConfService(getGenesysCli().loadEnvironment())
        )
    }

    override fun getGenesysCli() = config!!.getGenesysCli()

    fun exportConfiguration(processor: ExportProcessor, service: ConfService) {
        try {
            service.open()
            processor.begin()

            ConfigurationObjectType.values().forEach {
                val type = it
                info { "Exporting '${type.getObjectType()}' objects" }

                processor.beginType(type)

                val configurationObjects = service.retrieveMultipleObjects(
                    CfgObject::class.java,
                    it.queryType.createInstance()
                ) ?: emptyList()

                debug { "Found ${configurationObjects.size} ${type.getObjectType()} objects." }

                configurationObjects.forEach {
                    processor.processObject(type, it)
                }

                processor.endType(type)
            }

            processor.end()
        } catch (exception: Exception) {
            throw ExportException("Error occured while exporting configuration.", exception)
        } finally {
            service.close()
        }
    }
}

class ExportException(message: String, cause: Throwable) : Exception(message, cause)
