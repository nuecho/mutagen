package com.nuecho.genesys.cli.commands.config.export

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgConnInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.nuecho.genesys.cli.TestResources.loadRawConfiguration
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.ConfService
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.ByteArrayOutputStream

class RawExportProcessorTest : StringSpec() {
    private val service = ConfService(Environment(host = "test", user = "test", rawPassword = "test"))

    init {
        "exporting multiple objects of the same type should generate an ordered result" {
            val dn1 = CfgDN(service)
            val dn2 = CfgDN(service)
            val dn3 = CfgDN(service)

            dn1.number = "5555"
            dn2.number = "3333"
            dn3.number = "1111"

            checkExportOutput("sorted_dn.json", CfgObjectType.CFGDN, dn1, dn2, dn3)
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
        val output = ByteArrayOutputStream()
        val processor = RawExportProcessor(output)

        processor.begin()
        processor.beginType(type)
        objects.forEach { processor.processObject(it) }
        processor.endType(type)
        processor.end()

        val result = defaultJsonObjectMapper().readTree(String(output.toByteArray()))
        result shouldBe loadRawConfiguration("commands/config/export/raw/$expectedOutputFile")
    }
}
