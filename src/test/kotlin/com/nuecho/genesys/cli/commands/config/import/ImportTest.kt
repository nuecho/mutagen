package com.nuecho.genesys.cli.commands.config.import

import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import com.nuecho.genesys.cli.commands.config.ConfigMocks.mockMetadata
import com.nuecho.genesys.cli.commands.config.export.ExportFormat.JSON
import com.nuecho.genesys.cli.commands.config.import.Import.Companion.importConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationBuilder
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import io.kotlintest.matchers.should
import io.kotlintest.matchers.startWith
import io.kotlintest.specs.StringSpec
import io.mockk.objectMockk
import io.mockk.use
import io.mockk.verify

private const val USAGE_PREFIX = "Usage: import [-?]"

class ImportTest : StringSpec() {
    init {
        "executing Import with -h argument should print usage" {
            val output = execute("config", "import", "-h")
            output should startWith(USAGE_PREFIX)
        }

        "importing empty configuration should do nothing" {
            val metadata = mockMetadata(JSON)
            val configuration = ConfigurationBuilder().build(metadata)
            val service = mockConfService()

            objectMockk(Import.Companion).use {
                importConfiguration(configuration, service)
                verify(exactly = 0) { Import.save(any()) }
            }
        }
    }
}
