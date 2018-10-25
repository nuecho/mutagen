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

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.configuration.protocol.types.CfgFlag.CFGFalse
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPerson
import com.nuecho.mutagen.cli.TestResources.loadJsonConfiguration
import com.nuecho.mutagen.cli.commands.config.ConfigMocks.mockMetadata
import com.nuecho.mutagen.cli.commands.config.export.ExportFormat.COMPACT_JSON
import com.nuecho.mutagen.cli.commands.config.export.ExportFormat.JSON
import com.nuecho.mutagen.cli.core.compactJsonGenerator
import com.nuecho.mutagen.cli.core.compactJsonObjectMapper
import com.nuecho.mutagen.cli.core.defaultJsonGenerator
import com.nuecho.mutagen.cli.core.defaultJsonObjectMapper
import com.nuecho.mutagen.cli.models.configuration.Configuration
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_NAME
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.mutagen.cli.services.ServiceMocks.mockConfService
import io.mockk.every
import io.mockk.spyk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class JsonExportProcessorTest {
    private val service = mockConfService()

    @Test
    fun `exporting an empty list of configuration objects should yield an empty configuration`() {
        checkExportOutput("empty_configuration.json", CFGPerson)
    }

    @Test
    fun `exporting using compact JSON format should yield a compacted file`() {
        val tenant = mockCfgTenant(DEFAULT_TENANT_NAME)

        val person = spyk(CfgPerson(service))
        every { person.tenant } returns tenant
        person.employeeID = "333"
        person.userName = "pdeschen"
        person.isAgent = CFGFalse
        person.appRanks = emptyList()

        checkCompactExportOutput("compact_configuration.json", CFGPerson, person)
    }

    @Test
    fun `exporting multiple objects of the same type should generate an ordered result`() {
        val tenant = mockCfgTenant(DEFAULT_TENANT_NAME)

        val person1 = spyk(CfgPerson(service))
        every { person1.tenant } returns tenant
        person1.employeeID = "333"
        person1.userName = "pdeschen"
        person1.isAgent = CFGFalse

        val person2 = spyk(CfgPerson(service))
        every { person2.tenant } returns tenant
        person2.employeeID = "222"
        person2.userName = "fparga"
        person2.isAgent = CFGFalse

        val person3 = spyk(CfgPerson(service))
        every { person3.tenant } returns tenant
        person3.employeeID = "111"
        person3.userName = "dmorand"
        person3.isAgent = CFGFalse

        checkExportOutput("sorted_persons.json", CFGPerson, person1, person2, person3)
    }
}

private fun checkExportOutput(
    expectedOutputFile: String,
    type: CfgObjectType,
    vararg objects: ICfgObject
) {
    val metadata = mockMetadata(JSON)
    val output = ByteArrayOutputStream()
    val processor = JsonExportProcessor(metadata, defaultJsonGenerator(output))

    processObjects(processor, type, objects)

    val result = defaultJsonObjectMapper().readValue(String(output.toByteArray()), Configuration::class.java)
    assertThat(result, equalTo(loadJsonConfiguration("commands/config/export/json/$expectedOutputFile")))
}

private fun checkCompactExportOutput(
    expectedOutputFile: String,
    type: CfgObjectType,
    vararg objects: ICfgObject
) {
    val metadata = mockMetadata(COMPACT_JSON)
    val output = ByteArrayOutputStream()
    val processor = JsonExportProcessor(metadata, compactJsonGenerator(output))

    processObjects(processor, type, objects)

    val result = compactJsonObjectMapper().readValue(String(output.toByteArray()), Configuration::class.java)
    assertThat(result, equalTo(loadJsonConfiguration("commands/config/export/json/$expectedOutputFile")))
}

private fun processObjects(processor: JsonExportProcessor, type: CfgObjectType, objects: Array<out ICfgObject>) {
    processor.begin()
    processor.beginType(type)
    objects.forEach { processor.processObject(it) }
    processor.endType(type)
    processor.end()
}
