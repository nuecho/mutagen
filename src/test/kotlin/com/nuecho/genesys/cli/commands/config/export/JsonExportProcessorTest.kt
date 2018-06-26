package com.nuecho.genesys.cli.commands.config.export

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.configuration.protocol.types.CfgFlag.CFGFalse
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPerson
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.commands.config.ConfigMocks.mockMetadata
import com.nuecho.genesys.cli.commands.config.export.ExportFormat.COMPACT_JSON
import com.nuecho.genesys.cli.commands.config.export.ExportFormat.JSON
import com.nuecho.genesys.cli.core.compactJsonGenerator
import com.nuecho.genesys.cli.core.compactJsonObjectMapper
import com.nuecho.genesys.cli.core.defaultJsonGenerator
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.ConfService
import io.mockk.every
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class JsonExportProcessorTest {
    private val service = ConfService(Environment(host = "test", user = "test", rawPassword = "test"))

    @Test
    fun `exporting an empty list of configuration objects should yield an empty configuration`() {
        checkExportOutput("empty_configuration.json", CFGPerson)
    }

    @Test
    fun `exporting using compact JSON format should yield a compacted file`() {
        val tenant = mockCfgTenant(DEFAULT_TENANT)

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
        val tenant = mockCfgTenant(DEFAULT_TENANT)

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
    assertEquals(result, loadJsonConfiguration("commands/config/export/json/$expectedOutputFile"))
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
    assertEquals(result, loadJsonConfiguration("commands/config/export/json/$expectedOutputFile"))
}

private fun processObjects(processor: JsonExportProcessor, type: CfgObjectType, objects: Array<out ICfgObject>) {
    processor.begin()
    processor.beginType(type)
    objects.forEach { processor.processObject(it) }
    processor.endType(type)
    processor.end()
}
