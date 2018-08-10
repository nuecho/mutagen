package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgScriptType.CFGBusinessProcess
import com.genesyslab.platform.configuration.protocol.types.CfgScriptType.CFGSchedule
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgScriptType
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val NAME = "name"
private val script = Script(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    type = CFGBusinessProcess.toShortName(),
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class ScriptTest : ConfigurationObjectTest(
    configurationObject = script,
    emptyConfigurationObject = Script(tenant = DEFAULT_TENANT_REFERENCE, name = NAME),
    mandatoryProperties = setOf(TYPE),
    importedConfigurationObject = Script(mockCfgScript())
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(script.tenant)
            .add(script.folder)
            .toSet()

        assertThat(script.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() {
        val cfgScript = mockCfgScript(name = script.name).also {
            every { it.type } returns CFGSchedule
        }

        assertThat(configurationObject.checkUnchangeableProperties(cfgScript), equalTo(setOf(TYPE)))
    }

    @Test
    fun `createCfgObject should properly create CfgScript`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgScript::class.java, any()) } returns null
        mockRetrieveTenant(service)

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgScript = script.createCfgObject(service)

            with(cfgScript) {
                assertThat(name, equalTo(script.name))
                assertThat(index, equalTo(script.index))
                assertThat(state, equalTo(toCfgObjectState(script.state)))
                assertThat(type, equalTo(toCfgScriptType(script.type)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(script.userProperties))
            }
        }
    }
}

private fun mockCfgScript() = mockCfgScript(script.name).apply {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    every { configurationService } returns service
    every { state } returns CFGEnabled
    every { type } returns toCfgScriptType(script.type)
    every { index } returns script.index
    every { resources } returns null
    every { userProperties } returns mockKeyValueCollection()
    every { folderId } returns DEFAULT_OBJECT_DBID
}
