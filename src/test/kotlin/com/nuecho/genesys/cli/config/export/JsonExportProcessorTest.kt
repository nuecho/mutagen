package com.nuecho.genesys.cli.config.export

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.configuration.protocol.types.CfgAppType.CFGConfigServer
import com.nuecho.genesys.cli.GenesysServices.createConfigurationService
import com.nuecho.genesys.cli.TestResources.loadConfiguration
import com.nuecho.genesys.cli.config.ConfigurationObjectType
import com.nuecho.genesys.cli.config.ConfigurationObjectType.DN
import com.nuecho.genesys.cli.preferences.Environment
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.ByteArrayOutputStream

class JsonExportProcessorTest : StringSpec() {
    init {
        "exporting multiple objects of the same type should generate an ordered result" {
            val output = ByteArrayOutputStream()
            val processor = JsonExportProcessor(output)

            val environment = Environment(host = "test", user = "test", password = "test")
            val service = createConfigurationService(environment, CFGConfigServer)

            val dn1 = CfgDN(service)
            val dn2 = CfgDN(service)
            val dn3 = CfgDN(service)

            dn1.number = "5555"
            dn2.number = "3333"
            dn3.number = "1111"

            processObjects(processor, DN, dn1, dn2, dn3)
            String(output.toByteArray()) shouldBe loadConfiguration("sorted_dn.json")
        }
    }

    private fun processObjects(processor: ExportProcessor, type: ConfigurationObjectType, vararg objects: ICfgObject) {
        processor.begin()
        processor.beginType(type)
        objects.forEach { processor.processObject(type, it) }
        processor.endType(type)
        processor.end()
    }
}
