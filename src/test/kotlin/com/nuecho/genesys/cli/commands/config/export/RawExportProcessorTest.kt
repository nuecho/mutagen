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
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.ConfService
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.spyk
import java.io.ByteArrayOutputStream

class RawExportProcessorTest : StringSpec() {
    private val service = ConfService(Environment(host = "test", user = "test", rawPassword = "test"))

    init {
        "exporting multiple objects of the same type should generate an ordered result by dbid" {
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

        "exporting an object with array properties should properly serialize all array elements" {
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
        val processor = RawExportProcessor(output, metadata)

        processor.begin()
        processor.beginType(type)
        objects.forEach { processor.processObject(it) }
        processor.endType(type)
        processor.end()

        val result = defaultJsonObjectMapper().readTree(String(output.toByteArray()))
        result shouldBe loadRawConfiguration("commands/config/export/raw/$expectedOutputFile")
    }
}
