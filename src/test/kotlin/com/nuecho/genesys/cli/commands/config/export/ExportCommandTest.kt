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
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

private const val USAGE_PREFIX = "Usage: export [-?]"

class ExportCommandTest {
    @Test
    fun `executing Export with -h argument should print usage`() {
        val output = execute("config", "export", "-h")

        assertThat(output, startsWith(USAGE_PREFIX))
    }

    @Test
    fun `exporting raw empty configuration should generate an empty JSON array for each object type`() {
        val output = ByteArrayOutputStream()
        val processor = RawExportProcessor(mockMetadata(RAW), output)
        val service = mockConfService()

        exportConfiguration(processor, service)

        val result = defaultJsonObjectMapper().readTree(String(output.toByteArray()))
        assertThat(result, equalTo(loadRawConfiguration("commands/config/export/raw/empty_configuration.json")))
    }

    @Test
    fun `failing while exporting should result in an ExportException`() {
        val processor = mockk<ExportProcessor>()
        every { processor.begin() } throws RuntimeException()
        val service = mockConfService()

        assertThrows(ExportException::class.java) {
            exportConfiguration(processor, service)
        }
    }

    @Test
    fun `createExportProcessor should properly create Metadata based on ExpoetFormat`() {
        val environment = Environment(host = "host", user = "user", rawPassword = "password")

        val rawExportProcessor = createExportProcessor(RAW, environment, System.out) as RawExportProcessor
        assertThat(rawExportProcessor.metadata.formatName, equalTo(RAW.name))
        assertThat(rawExportProcessor.metadata.formatVersion, equalTo(RAW.version))

        val jsonExportProcessor = createExportProcessor(JSON, environment, System.out) as JsonExportProcessor
        assertThat(jsonExportProcessor.metadata.formatName, equalTo(JSON.name))
        assertThat(jsonExportProcessor.metadata.formatVersion, equalTo(JSON.version))
    }
}

private fun mockConfService(configuration: Map<ICfgQuery<ICfgObject>, Collection<ICfgObject>> = emptyMap()): ConfService {
    val confService = mockk<ConfService>()
    every { confService.open() } just Runs
    every { confService.close() } just Runs

    val query = slot<ICfgQuery<ICfgObject>>()
    every { confService.retrieveMultipleObjects<ICfgObject>(any(), capture(query)) } answers {
        configuration[query.captured]
    }

    return confService
}
