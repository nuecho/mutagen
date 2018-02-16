package com.nuecho.genesys.cli.config.export

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nuecho.genesys.cli.GenesysCliCommandTest
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.config.TestConfigurationService
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.matchers.startWith
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayOutputStream

private const val USAGE_PREFIX = "Usage: export [-?]"

class ExportTest : GenesysCliCommandTest() {
    init {
        "executing Export with -h argument should print usage" {
            val output = execute("config", "export", "-h")
            output should startWith(USAGE_PREFIX)
        }

        "exporting empty configuration should generate an empty JSON array for each object type" {
            val output = ByteArrayOutputStream()
            val processor = JsonExportProcessor(output)
            val service = TestConfigurationService(emptyMap())
            Export().exportConfiguration(processor, service)

            val result = jacksonObjectMapper().readTree(String(output.toByteArray()))
            result shouldBe loadJsonConfiguration("empty_configuration.json")
        }

        "failing while exporting should result in an ExportException" {
            val processor = mockk<ExportProcessor>()
            every {
                processor.begin()
            } throws RuntimeException()

            val service = TestConfigurationService(emptyMap())

            shouldThrow<ExportException> {
                Export().exportConfiguration(processor, service)
            }
        }
    }
}
