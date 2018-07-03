package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumeratorValue
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgEnumeratorValue
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.reference.EnumeratorReference
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveEnumerator
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.staticMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val NAME = "enumeratorValue"
private const val ENUMERATOR = "enumerator"
private val ENUMERATOR_REFERENCE = EnumeratorReference(ENUMERATOR, DEFAULT_TENANT_REFERENCE)
private const val DESCRIPTION = "description"
private const val DISPLAY_NAME = "displayName"

private val enumeratorValue = EnumeratorValue(
    tenant = DEFAULT_TENANT_REFERENCE,
    enumerator = ENUMERATOR_REFERENCE,
    name = NAME,
    description = DESCRIPTION,
    displayName = DISPLAY_NAME,
    default = false,
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = ConfigurationTestData.defaultProperties()
)

class EnumeratorValueTest : NoImportedObjectConfigurationObjectTest(
    enumeratorValue,
    EnumeratorValue(
        enumerator = ENUMERATOR_REFERENCE,
        name = NAME
    ),
    setOf(DISPLAY_NAME, TENANT)
) {

    @Test
    fun `CfgEnumeratorValue initialized EnumeratorValue should properly serialize`() {
        val service = mockConfService()
        mockRetrieveEnumerator(service, ENUMERATOR)

        val enumeratorValue = EnumeratorValue(mockCfgEnumeratorValue(service))
        checkSerialization(enumeratorValue, "enumeratorvalue")
    }

    @Test
    fun `updateCfgObject should properly create CfgEnumeratorValue`() {

        staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
            val service = mockConfService()
            every { service.getObjectDbid(any()) } answers { DEFAULT_OBJECT_DBID }
            every { service.retrieveObject(CfgEnumeratorValue::class.java, any()) } returns null
            mockRetrieveEnumerator(service, ENUMERATOR)

            val cfgEnumeratorValue = enumeratorValue.updateCfgObject(service)

            with(cfgEnumeratorValue) {
                assertThat(name, equalTo(enumeratorValue.name))
                assertThat(displayName, equalTo(enumeratorValue.displayName))
                assertThat(description, equalTo(enumeratorValue.description))
                assertThat(enumeratorDBID, equalTo(DEFAULT_OBJECT_DBID))
                assertThat(isDefault, equalTo(toCfgFlag(enumeratorValue.default)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(enumeratorValue.userProperties))
                assertThat(state, equalTo(toCfgObjectState(enumeratorValue.state)))
            }
        }
    }
}

private fun mockCfgEnumeratorValue(service: IConfService): CfgEnumeratorValue {
    val cfgEnumeratorValue = mockCfgEnumeratorValue(name = NAME)
    val userPropertiesMock = mockKeyValueCollection()

    return cfgEnumeratorValue.apply {
        every { configurationService } returns service
        every { description } returns DESCRIPTION
        every { displayName } returns DISPLAY_NAME
        every { enumeratorDBID } returns DEFAULT_OBJECT_DBID
        every { isDefault } returns CfgFlag.CFGFalse
        every { tenant.name } returns "tenant"
        every { userProperties } returns userPropertiesMock
        every { state } returns CFGEnabled
    }
}
