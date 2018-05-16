package com.nuecho.genesys.cli.commands.config.export

import com.genesyslab.platform.applicationblocks.com.CfgFilterBasedQuery
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.queries.CfgTenantQuery
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPersonLastLogin
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging.debug
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.commands.config.Config
import com.nuecho.genesys.cli.commands.config.export.Export.createExportProcessor
import com.nuecho.genesys.cli.commands.config.export.Export.exportConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects
import com.nuecho.genesys.cli.models.configuration.Metadata
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.ConfService
import picocli.CommandLine
import java.io.OutputStream

@CommandLine.Command(
    name = "export",
    description = ["Export configuration objects."]
)
class ExportCommand : GenesysCliCommand() {
    @CommandLine.ParentCommand
    private var config: Config? = null

    @CommandLine.Option(
        names = ["--format"],
        description = ["Export format [RAW|JSON]."]
    )
    private var format: ExportFormat? = ExportFormat.RAW

    override fun execute() {
        val environment = getGenesysCli().loadEnvironment()

        exportConfiguration(
            createExportProcessor(format!!, environment, System.out),
            ConfService(environment)
        )
    }

    override fun getGenesysCli() = config!!.getGenesysCli()
}

object Export {
    fun exportConfiguration(processor: ExportProcessor, service: ConfService) {
        try {
            service.open()
            processor.begin()

            val types = ConfigurationObjects.getCfgObjectTypes()
            val excludedTypes = listOf(CFGPersonLastLogin)

            types
                .filter { !excludedTypes.contains(it) }
                .sortedBy { it.name() }
                .forEach { processObjectType(it, processor, service) }

            processor.end()
        } catch (exception: Exception) {
            throw ExportException("Error occured while exporting configuration.", exception)
        } finally {
            service.close()
        }
    }

    internal fun createExportProcessor(format: ExportFormat, environment: Environment, output: OutputStream) =
        Metadata.create(format, environment).let {
            when (format) {
                ExportFormat.RAW -> RawExportProcessor(output, it)
                ExportFormat.JSON -> JsonExportProcessor(output, it)
            }
        }

    private fun processObjectType(type: CfgObjectType, processor: ExportProcessor, service: ConfService) {
        info { "Exporting '$type' objects" }

        processor.beginType(type)

        val query = when (type) {
        // Using a CfgTenantQuery with allTenants is necessary to get the CfgTenant with DBID=1
            CfgObjectType.CFGTenant -> CfgTenantQuery().apply { allTenants = 1 }
            else -> CfgFilterBasedQuery(type)
        }

        val configurationObjects = service.retrieveMultipleObjects(
            CfgObject::class.java,
            query
        ) ?: emptyList()

        debug { "Found ${configurationObjects.size} $type objects." }

        configurationObjects.forEach {
            processor.processObject(it)
        }

        processor.endType(type)
    }
}

class ExportException(message: String, cause: Throwable) : Exception(message, cause)
