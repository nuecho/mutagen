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
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgConnInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.nuecho.genesys.cli.TestResources.loadRawConfiguration
import com.nuecho.genesys.cli.commands.config.ConfigMocks.mockMetadata
import com.nuecho.genesys.cli.commands.config.export.ExportFormat.RAW
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import io.mockk.every
import io.mockk.spyk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class RawExportProcessorTest {
    private val service = mockConfService()

    @Test
    fun `exporting multiple objects of the same type should generate an ordered result by dbid`() {
        val switch1 = spyk(CfgSwitch(service)).apply {
            every { objectDbid } returns 101
            name = "aaa"
        }
        val switch2 = spyk(CfgSwitch(service)).apply {
            every { objectDbid } returns 102
            name = "bbb"
        }
        val switch3 = spyk(CfgSwitch(service)).apply {
            every { objectDbid } returns 103
            name = "ccc"
        }

        checkExportOutput("sorted_switch.json", CfgObjectType.CFGSwitch, switch3, switch2, switch1)
    }

    @Test
    fun `exporting an object with array properties should properly serialize all array elements`() {
        val application = CfgApplication(service)
        application.name = "Application"

        val appServer1 = CfgConnInfo(service, application)
        appServer1.id = "appServer1"

        val appServer2 = CfgConnInfo(service, application)
        appServer2.id = "appServer2"

        application.appServers = listOf(appServer1, appServer2)

        checkExportOutput("application.json", CfgObjectType.CFGApplication, application)
    }
}

private fun checkExportOutput(
    expectedOutputFile: String,
    type: CfgObjectType,
    vararg objects: ICfgObject
) {
    val metadata = mockMetadata(RAW)
    val output = ByteArrayOutputStream()
    val processor = RawExportProcessor(metadata, output)

    processor.begin()
    processor.beginType(type)
    objects.forEach { processor.processObject(it) }
    processor.endType(type)
    processor.end()

    val result = defaultJsonObjectMapper().readTree(String(output.toByteArray()))
    assertThat(result, equalTo(loadRawConfiguration("commands/config/export/raw/$expectedOutputFile")))
}
