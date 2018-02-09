package com.nuecho.genesys.cli.config.export

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nuecho.genesys.cli.CommandTest
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.config.TestConfigurationService
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.startWith
import java.io.ByteArrayOutputStream

class ExportTest : CommandTest() {
    init {
        val usagePrefix = "Usage: export"

        "executing Export with -h argument should print usage" {
            val output = execute("-h")
            output should startWith(usagePrefix)
        }

        "exporting empty configuration should generate an empty JSON array for each object type" {
            val output = ByteArrayOutputStream()
            val processor = JsonExportProcessor(output)
            val service = TestConfigurationService(emptyMap())
            Export().exportConfiguration(processor, service)

            val result = jacksonObjectMapper().readTree(String(output.toByteArray()))
            result shouldBe loadJsonConfiguration("empty_configuration.json")
        }
    }

    override fun createCommand(): Runnable {
        return Export()
    }
}
