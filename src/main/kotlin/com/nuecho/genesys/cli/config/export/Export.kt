package com.nuecho.genesys.cli.config.export

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging.debug
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.config.Config
import com.nuecho.genesys.cli.config.ConfigurationObjectType
import com.nuecho.genesys.cli.config.ConfigurationService
import com.nuecho.genesys.cli.config.RemoteConfigurationService
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
            RemoteConfigurationService(getGenesysCli().loadEnvironment())
        )
    }

    override fun getGenesysCli(): GenesysCli {
        return config!!.getGenesysCli()
    }

    fun exportConfiguration(processor: ExportProcessor, service: ConfigurationService) {
        try {
            service.connect()
            processor.begin()

            ConfigurationObjectType.values().forEach {
                val type = it
                info { "Exporting '${type.getObjectType()}' objects" }

                processor.beginType(type)

                val configurationObjects = service.retrieveMultipleObjects(
                    CfgObject::class.java,
                    it.queryType.createInstance()
                )

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
            service.disconnect()
        }
    }
}

class ExportException(message: String, cause: Throwable) : Exception(message, cause)
