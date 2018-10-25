/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nuecho.mutagen.cli.commands.config.export

import com.genesyslab.platform.applicationblocks.com.CfgFilterBasedQuery
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.queries.CfgTenantQuery
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPersonLastLogin
import com.nuecho.mutagen.cli.Logging.debug
import com.nuecho.mutagen.cli.Logging.info
import com.nuecho.mutagen.cli.commands.ConfigServerCommand
import com.nuecho.mutagen.cli.commands.config.Config
import com.nuecho.mutagen.cli.commands.config.export.Export.createExportProcessor
import com.nuecho.mutagen.cli.commands.config.export.Export.exportConfiguration
import com.nuecho.mutagen.cli.commands.config.export.ExportFormat.COMPACT_JSON
import com.nuecho.mutagen.cli.commands.config.export.ExportFormat.JSON
import com.nuecho.mutagen.cli.commands.config.export.ExportFormat.RAW
import com.nuecho.mutagen.cli.core.MetricNames.CONFIG_EXPORT
import com.nuecho.mutagen.cli.core.MetricNames.CONFIG_EXPORT_PROCESS
import com.nuecho.mutagen.cli.core.MetricNames.CONFIG_EXPORT_RETRIEVE
import com.nuecho.mutagen.cli.core.Metrics.time
import com.nuecho.mutagen.cli.core.compactJsonGenerator
import com.nuecho.mutagen.cli.core.defaultJsonGenerator
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects
import com.nuecho.mutagen.cli.models.configuration.Metadata
import com.nuecho.mutagen.cli.preferences.environment.Environment
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.services.ConfigurationObjectRepository.prefetchConfigurationObjects
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

    override fun getMutagenCli() = config!!.getMutagenCli()
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
