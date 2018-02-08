package com.nuecho.genesys.cli.config.export

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.config.ConfigurationObjectType
import com.nuecho.genesys.cli.config.ConfigurationService
import com.nuecho.genesys.cli.config.RemoteConfigurationService
import picocli.CommandLine
import kotlin.reflect.full.createInstance

@CommandLine.Command(
    name = "export",
    description = ["Genesys Config Server export tool"]
)
class Export : GenesysCliCommand(), Runnable {
    override fun run() {
        val service = RemoteConfigurationService(connect())

        try {
            exportConfiguration(JsonExportProcessor(System.out), service)
        } finally {
            service.release()
        }
    }

    fun exportConfiguration(processor: ExportProcessor, service: ConfigurationService) {
        processor.begin()

        ConfigurationObjectType.values().forEach {
            val type = it
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
