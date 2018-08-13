package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.configuration.protocol.types.CfgEnumeratorType.CFGENTRole
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgEnumerator
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgEnumeratorType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
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
private val enumerator = Enumerator(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    displayName = "displayName",
    description = "description",
    type = CFGENTRole.toShortName(),
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class EnumeratorTest : ConfigurationObjectTest(
    configurationObject = enumerator,
    emptyConfigurationObject = Enumerator(tenant = DEFAULT_TENANT_REFERENCE, name = NAME),
    mandatoryProperties = setOf(DISPLAY_NAME, TYPE),
    importedConfigurationObject = Enumerator(mockCfgEnumerator())
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(enumerator.tenant)
            .add(enumerator.folder)
            .toSet()

        assertThat(enumerator.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        assertUnchangeableProperties(mockCfgEnumerator(), FOLDER)

    @Test
    fun `createCfgObject should properly create CfgEnumerator`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgEnumerator::class.java, any()) } returns null
        mockRetrieveTenant(service)

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgEnumerator = enumerator.createCfgObject(service)

            with(cfgEnumerator) {
                assertThat(name, equalTo(enumerator.name))
                assertThat(displayName, equalTo(enumerator.displayName))
                assertThat(description, equalTo(enumerator.description))
                assertThat(type, equalTo(toCfgEnumeratorType(enumerator.type)))
                assertThat(state, equalTo(toCfgObjectState(enumerator.state)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(enumerator.userProperties))
            }
        }
    }

    @Test
    fun `createCfgObject should use name when displayName is not specified`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgEnumerator::class.java, any()) } returns null
        mockRetrieveTenant(service)

        val cfgEnumerator = Enumerator(DEFAULT_TENANT_REFERENCE, NAME).createCfgObject(service)

        with(cfgEnumerator) {
            assertThat(name, equalTo(NAME))
            assertThat(displayName, equalTo(NAME))
        }
    }
}

private fun mockCfgEnumerator() = mockCfgEnumerator(enumerator.name).apply {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    every { configurationService } returns service
    every { displayName } returns enumerator.displayName
    every { description } returns enumerator.description
    every { type } returns toCfgEnumeratorType(enumerator.type)
    every { state } returns toCfgObjectState(enumerator.state)
    every { userProperties } returns mockKeyValueCollection()
    every { folderId } returns DEFAULT_FOLDER_DBID
}
