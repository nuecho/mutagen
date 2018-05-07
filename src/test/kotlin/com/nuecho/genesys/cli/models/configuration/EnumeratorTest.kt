package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.configuration.protocol.types.CfgEnumeratorType.CFGENTRole
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgEnumerator
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgEnumeratorType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every

private const val NAME = "name"
private val enumerator = Enumerator(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    displayName = "displayName",
    description = "description",
    type = CFGENTRole.toShortName(),
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties()
)

class EnumeratorTest : ConfigurationObjectTest(
    enumerator,
    Enumerator(tenant = DEFAULT_TENANT_REFERENCE, name = NAME),
    Enumerator(mockCfgEnumerator())
) {
    init {
        "Enumerator.updateCfgObject should properly create CfgEnumerator" {
            val service = mockConfService()
            every { service.retrieveObject(CfgEnumerator::class.java, any()) } returns null
            mockRetrieveTenant(service)

            val (status, cfgObject) = enumerator.updateCfgObject(service)
            val cfgEnumerator = cfgObject as CfgEnumerator

            status shouldBe ConfigurationObjectUpdateStatus.CREATED

            with(cfgEnumerator) {
                name shouldBe enumerator.name
                displayName shouldBe enumerator.displayName
                description shouldBe enumerator.description
                type shouldBe toCfgEnumeratorType(enumerator.type)
                state shouldBe toCfgObjectState(enumerator.state)
                userProperties.asCategorizedProperties() shouldBe enumerator.userProperties
            }
        }

        "Enumerator.updateCfgObject should use name when displayName is not specified" {
            val service = mockConfService()
            every { service.retrieveObject(CfgEnumerator::class.java, any()) } returns null
            mockRetrieveTenant(service)

            val (_, cfgObject) = Enumerator(DEFAULT_TENANT_REFERENCE, NAME).updateCfgObject(service)
            val cfgEnumerator = cfgObject as CfgEnumerator

            with(cfgEnumerator) {
                name shouldBe NAME
                displayName shouldBe NAME
            }
        }
    }
}

private fun mockCfgEnumerator() = mockCfgEnumerator(enumerator.name).apply {
    every { displayName } returns enumerator.displayName
    every { description } returns enumerator.description
    every { type } returns toCfgEnumeratorType(enumerator.type)
    every { state } returns toCfgObjectState(enumerator.state)
    every { userProperties } returns mockKeyValueCollection()
}
