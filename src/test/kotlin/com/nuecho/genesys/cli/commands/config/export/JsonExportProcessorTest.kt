package com.nuecho.genesys.cli.commands.config.export

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPerson
import com.nuecho.genesys.cli.TestResources
import com.nuecho.genesys.cli.commands.config.ConfigMocks.mockMetadata
import com.nuecho.genesys.cli.commands.config.export.ExportFormat.JSON
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.ConfService
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.ByteArrayOutputStream

class JsonExportProcessorTest : StringSpec() {
    private val service = ConfService(Environment(host = "test", user = "test", rawPassword = "test"))

    init {
        "exporting an empty list of configuration objects should yield an empty configuration" {
            checkExportOutput("empty_configuration.json", CFGPerson)
        }

        "exporting multiple objects of the same type should generate an ordered result" {
            val person1 = CfgPerson(service)
            person1.employeeID = "333"
            person1.userName = "pdeschen"

            val person2 = CfgPerson(service)
            person2.employeeID = "222"
            person2.userName = "fparga"

            val person3 = CfgPerson(service)
            person3.employeeID = "111"
            person3.userName = "dmorand"

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
        val processor = JsonExportProcessor(output, metadata)

        processor.begin()
        processor.beginType(type)
        objects.forEach { processor.processObject(it) }
        processor.endType(type)
        processor.end()

        val result = defaultJsonObjectMapper().readValue(String(output.toByteArray()), Configuration::class.java)
        result shouldBe TestResources.loadJsonConfiguration("commands/config/export/json/$expectedOutputFile")
    }
}
