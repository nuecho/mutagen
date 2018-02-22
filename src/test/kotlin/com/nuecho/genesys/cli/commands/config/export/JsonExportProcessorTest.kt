package com.nuecho.genesys.cli.commands.config.export

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgConnInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectType.APPLICATION
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectType.DN
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.ConfService
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.ByteArrayOutputStream

class JsonExportProcessorTest : StringSpec() {

    private val service = ConfService(Environment(host = "test", user = "test", password = "test"))

    init {
        "exporting multiple objects of the same type should generate an ordered result" {
            val dn1 = CfgDN(service)
            val dn2 = CfgDN(service)
            val dn3 = CfgDN(service)

            dn1.number = "5555"
            dn2.number = "3333"
            dn3.number = "1111"

            checkExportOutput("sorted_dn.json", DN, dn1, dn2, dn3)
        }

        "exporting an object with array properties should properly serialize all array elements" {
            val application = CfgApplication(service)
            application.name = "Application"

            val appServer1 = CfgConnInfo(service, application)
            appServer1.id = "appServer1"

            val appServer2 = CfgConnInfo(service, application)
            appServer2.id = "appServer2"

            application.appServers = listOf(appServer1, appServer2)

            checkExportOutput("application.json", APPLICATION, application)
        }
    }

    private fun checkExportOutput(
        expectedOutputFile: String,
        type: ConfigurationObjectType,
        vararg objects: ICfgObject
    ) {
        val output = ByteArrayOutputStream()
        val processor = JsonExportProcessor(output)

        processor.begin()
        processor.beginType(type)
        objects.forEach { processor.processObject(type, it) }
        processor.endType(type)
        processor.end()

        val result = jacksonObjectMapper().readTree(String(output.toByteArray()))
        result shouldBe loadJsonConfiguration(expectedOutputFile)
    }
}
