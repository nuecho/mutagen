package com.nuecho.genesys.cli.commands.config.export

import com.genesyslab.platform.applicationblocks.com.CfgFilterBasedQuery
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.queries.CfgTenantQuery
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPersonLastLogin
import com.nuecho.genesys.cli.Logging.debug
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.commands.ConfigServerCommand
import com.nuecho.genesys.cli.commands.config.Config
import com.nuecho.genesys.cli.commands.config.export.Export.createExportProcessor
import com.nuecho.genesys.cli.commands.config.export.Export.exportConfiguration
import com.nuecho.genesys.cli.commands.config.export.ExportFormat.COMPACT_JSON
import com.nuecho.genesys.cli.commands.config.export.ExportFormat.JSON
import com.nuecho.genesys.cli.commands.config.export.ExportFormat.RAW
import com.nuecho.genesys.cli.core.MetricNames.CONFIG_EXPORT
import com.nuecho.genesys.cli.core.MetricNames.CONFIG_EXPORT_PROCESS
import com.nuecho.genesys.cli.core.MetricNames.CONFIG_EXPORT_RETRIEVE
import com.nuecho.genesys.cli.core.Metrics.time
import com.nuecho.genesys.cli.core.compactJsonGenerator
import com.nuecho.genesys.cli.core.defaultJsonGenerator
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects
import com.nuecho.genesys.cli.models.configuration.Metadata
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository.prefetchConfigurationObjects
import picocli.CommandLine
import java.io.OutputStream

@CommandLine.Command(
    name = "export",
    description = ["Export configuration objects."]
)
class ExportCommand : ConfigServerCommand() {
    @CommandLine.ParentCommand
    private var config: Config? = null

    @CommandLine.Option(
        names = ["--format"],
        description = ["Export format [RAW|JSON|COMPACT_JSON]."]
    )
    private var format: ExportFormat? = RAW

    override fun execute(): Int {
        withEnvironmentConfService { service: ConfService, environment: Environment ->
            prefetchConfigurationObjects(service)

            time(CONFIG_EXPORT) {
                exportConfiguration(
                    createExportProcessor(format!!, environment, System.out),
                    service
                )
            }
        }

        return 0
    }

    override fun getGenesysCli() = config!!.getGenesysCli()
}

object Export {
    fun exportConfiguration(processor: ExportProcessor, service: ConfService) {
        try {
            processor.begin()

            val types = ConfigurationObjects.getCfgObjectTypes()
            val excludedTypes = listOf(CFGPersonLastLogin)

            types
                .filter { !excludedTypes.contains(it) }
                .sortedBy { it.name() }
                .forEach { processObjectType(it, processor, service) }

            processor.end()
        } catch (exception: Exception) {
            throw ExportException("Error occurred while exporting configuration.", exception)
        }
    }

    internal fun createExportProcessor(format: ExportFormat, environment: Environment, output: OutputStream) =
        Metadata.create(format, environment).let {
            when (format) {
                RAW -> RawExportProcessor(it, output)
                JSON -> JsonExportProcessor(it, defaultJsonGenerator(output))
                COMPACT_JSON -> JsonExportProcessor(it, compactJsonGenerator(output))
            }
        }

    private fun processObjectType(type: CfgObjectType, processor: ExportProcessor, service: ConfService) {
        info { "Exporting '$type' objects" }

        processor.beginType(type)

        val query = when (type) {
        // Using a CfgTenantQuery with allTenants is necessary to get the CfgTenant with DBID=1
            CfgObjectType.CFGTenant -> CfgTenantQuery().apply { allTenants = 1 }
            else -> CfgFilterBasedQuery<ICfgObject>(type)
        }

        val configurationObjects = time(CONFIG_EXPORT_RETRIEVE) {
            service.retrieveMultipleObjects(
                CfgObject::class.java,
                query
            ) ?: emptyList()
        }

        debug { "Found ${configurationObjects.size} $type objects." }

        time(CONFIG_EXPORT_PROCESS) {
            configurationObjects.forEach {
                processor.processObject(it)
            }
        }

        processor.endType(type)
    }
}

class ExportException(message: String, cause: Throwable) : Exception(message, cause)
