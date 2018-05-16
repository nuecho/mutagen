package com.nuecho.genesys.cli.commands.config.export

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgQuery
import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import com.nuecho.genesys.cli.TestResources.loadRawConfiguration
import com.nuecho.genesys.cli.commands.config.ConfigMocks.mockMetadata
import com.nuecho.genesys.cli.commands.config.export.Export.createExportProcessor
import com.nuecho.genesys.cli.commands.config.export.Export.exportConfiguration
import com.nuecho.genesys.cli.commands.config.export.ExportFormat.JSON
import com.nuecho.genesys.cli.commands.config.export.ExportFormat.RAW
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.ConfService
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.matchers.startWith
import io.kotlintest.specs.StringSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import java.io.ByteArrayOutputStream

private const val USAGE_PREFIX = "Usage: export [-?]"

class ExportCommandTest : StringSpec() {
    init {
        "executing Export with -h argument should print usage" {
            val output = execute("config", "export", "-h")

            output should startWith(USAGE_PREFIX)
        }

        "exporting raw empty configuration should generate an empty JSON array for each object type" {
            val output = ByteArrayOutputStream()
            val processor = RawExportProcessor(output, mockMetadata(RAW))
            val service = mockConfService()

            exportConfiguration(processor, service)

            val result = defaultJsonObjectMapper().readTree(String(output.toByteArray()))
            result shouldBe loadRawConfiguration("commands/config/export/raw/empty_configuration.json")
        }

        "failing while exporting should result in an ExportException" {
            val processor = mockk<ExportProcessor>()
            every { processor.begin() } throws RuntimeException()
            val service = mockConfService()

            shouldThrow<ExportException> {
                exportConfiguration(processor, service)
            }
        }

        "createExportProcessor should properly create Metadata based on ExpoetFormat" {
            val environment = Environment(host = "host", user = "user", rawPassword = "password")

            val rawExportProcessor = createExportProcessor(RAW, environment, System.out) as RawExportProcessor
            rawExportProcessor.metadata.formatName shouldBe RAW.name
            rawExportProcessor.metadata.formatVersion shouldBe RAW.version

            val jsonExportProcessor = createExportProcessor(JSON, environment, System.out) as JsonExportProcessor
            jsonExportProcessor.metadata.formatName shouldBe JSON.name
            jsonExportProcessor.metadata.formatVersion shouldBe JSON.version
        }
    }
}

private fun mockConfService(configuration: Map<ICfgQuery, Collection<ICfgObject>> = emptyMap()): ConfService {
    val confService = mockk<ConfService>()
    every { confService.open() } just Runs
    every { confService.close() } just Runs

    val query = slot<ICfgQuery>()
    every { confService.retrieveMultipleObjects<ICfgObject>(any(), capture(query)) } answers {
        configuration[query.captured]
    }

    return confService
}
