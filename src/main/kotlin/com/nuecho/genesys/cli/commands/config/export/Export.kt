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
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects
import com.nuecho.genesys.cli.services.ConfService
import picocli.CommandLine
import java.io.OutputStream

@CommandLine.Command(
    name = "export",
    description = ["Export configuration objects."]
)
class Export : GenesysCliCommand() {
    @CommandLine.ParentCommand
    private var config: Config? = null

    @CommandLine.Option(
        names = ["-f", "--format"],
        description = ["Export format [RAW|JSON]."]
    )
    private var format: ExportProcessorType? = ExportProcessorType.RAW

    override fun execute() {
        exportConfiguration(
            createExportProcessor(System.out),
            ConfService(getGenesysCli().loadEnvironment())
        )
    }

    override fun getGenesysCli() = config!!.getGenesysCli()

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

    private fun createExportProcessor(output: OutputStream) =
        when (format) {
            ExportProcessorType.RAW -> RawExportProcessor(output)
            ExportProcessorType.JSON -> JsonExportProcessor(output)
            else -> throw IllegalArgumentException("Illegal export format value: '$format'")
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
