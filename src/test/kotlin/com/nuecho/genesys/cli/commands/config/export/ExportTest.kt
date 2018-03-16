package com.nuecho.genesys.cli.commands.config.export

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgQuery
import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import com.nuecho.genesys.cli.TestResources.loadRawConfiguration
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

class ExportTest : StringSpec() {
    init {
        "executing Export with -h argument should print usage" {
            val output = execute("config", "export", "-h")

            output should startWith(USAGE_PREFIX)
        }

        "exporting raw empty configuration should generate an empty JSON array for each object type" {
            val output = ByteArrayOutputStream()
            val processor = RawExportProcessor(output)
            val service = mockConfService()

            Export().exportConfiguration(processor, service)

            val result = jacksonObjectMapper().readTree(String(output.toByteArray()))
            result shouldBe loadRawConfiguration("commands/config/export/raw/empty_configuration.json")
        }

        "failing while exporting should result in an ExportException" {
            val processor = mockk<ExportProcessor>()
            every { processor.begin() } throws RuntimeException()
            val service = mockConfService()

            shouldThrow<ExportException> {
                Export().exportConfiguration(processor, service)
            }
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
