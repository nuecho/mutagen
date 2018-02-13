package com.nuecho.genesys.cli.config.export

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
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
        val service = RemoteConfigurationService(getGenesysCli().connect())

        try {
            exportConfiguration(JsonExportProcessor(System.out), service)
        } finally {
            service.release()
        }
    }

    override fun getGenesysCli(): GenesysCli {
        return config!!.getGenesysCli()
    }

    fun exportConfiguration(processor: ExportProcessor, service: ConfigurationService) {
        processor.begin()

        ConfigurationObjectType.values().forEach {
            val type = it
            info { "Exporting '${type.getObjectType()}'..." }
            processor.beginType(type)

            service.retrieveMultipleObjects(
                CfgObject::class.java,
                it.queryType.createInstance()
            ).forEach {
                processor.processObject(type, it)
            }

            processor.endType(type)
        }

        processor.end()
    }
}
