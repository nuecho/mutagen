package com.nuecho.genesys.cli.config.export

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgConnInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.configuration.protocol.types.CfgAppType.CFGConfigServer
import com.nuecho.genesys.cli.GenesysServices.createConfigurationService
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.config.ConfigurationObjectType
import com.nuecho.genesys.cli.config.ConfigurationObjectType.APPLICATION
import com.nuecho.genesys.cli.config.ConfigurationObjectType.DN
import com.nuecho.genesys.cli.preferences.environment.Environment
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.ByteArrayOutputStream

class JsonExportProcessorTest : StringSpec() {
    init {
        "exporting multiple objects of the same type should generate an ordered result" {
            val service = createConfigurationService()

            val dn1 = CfgDN(service)
            val dn2 = CfgDN(service)
            val dn3 = CfgDN(service)

            dn1.number = "5555"
            dn2.number = "3333"
            dn3.number = "1111"

            checkExportOutput("sorted_dn.json", DN, dn1, dn2, dn3)
        }

        "exporting an object with array properties should properly serialize all array elements" {
            val service = createConfigurationService()

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

    private fun createConfigurationService(): IConfService {
        val environment = Environment(host = "test", user = "test", password = "test")
        return createConfigurationService(environment, CFGConfigServer)
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
