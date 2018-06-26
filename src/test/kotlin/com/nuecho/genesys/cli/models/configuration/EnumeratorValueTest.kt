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
import org.junit.jupiter.api.Assertions.assertEquals
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

class EnumeratorValueTest : GroupConfigurationObjectTest(
    enumeratorValue,
    EnumeratorValue(
        default = false,
        displayName = "displayName",
        enumerator = ENUMERATOR_REFERENCE,
        name = NAME
    )
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
                assertEquals(name, enumeratorValue.name)
                assertEquals(displayName, enumeratorValue.displayName)
                assertEquals(description, enumeratorValue.description)
                assertEquals(enumeratorDBID, DEFAULT_OBJECT_DBID)
                assertEquals(isDefault, toCfgFlag(enumeratorValue.default))
                assertEquals(userProperties.asCategorizedProperties(), enumeratorValue.userProperties)
                assertEquals(state, toCfgObjectState(enumeratorValue.state))
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
