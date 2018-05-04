package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.configuration.protocol.types.CfgEnumeratorType.CFGENTRole
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgEnumerator
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgEnumeratorType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.retrieveEnumerator
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every
import io.mockk.staticMockk
import io.mockk.use

private const val NAME = "name"
private val enumerator = Enumerator(
    name = NAME,
    displayName = "displayName",
    description = "description",
    type = CFGENTRole.toShortName(),
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties()
)

class EnumeratorTest : ConfigurationObjectTest(enumerator, Enumerator(NAME), Enumerator(mockCfgEnumerator())) {
    init {
        val service = mockConfService()

        "Enumerator.updateCfgObject should properly create CfgEnumerator" {
            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {

                every { service.retrieveEnumerator(any()) } returns null

                val (status, cfgObject) = enumerator.updateCfgObject(service)
                val cfgEnumerator = cfgObject as CfgEnumerator

                status shouldBe ConfigurationObjectUpdateStatus.CREATED

                with(cfgEnumerator) {
                    name shouldBe enumerator.name
                    displayName shouldBe enumerator.displayName
                    description shouldBe enumerator.description
                    type shouldBe toCfgEnumeratorType(enumerator.type)
                    state shouldBe toCfgObjectState(enumerator.state)
                    userProperties.size shouldBe 4
                }
            }
        }

        "Enumerator.updateCfgObject should use name when displayName is not specified" {
            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
                every { service.retrieveEnumerator(any()) } returns null

                val (_, cfgObject) = Enumerator(NAME).updateCfgObject(service)
                val cfgEnumerator = cfgObject as CfgEnumerator

                with(cfgEnumerator) {
                    name shouldBe NAME
                    displayName shouldBe NAME
                }
            }
        }
    }
}

private fun mockCfgEnumerator() = mockCfgEnumerator(enumerator.name).also {
    every { it.displayName } returns enumerator.displayName
    every { it.description } returns enumerator.description
    every { it.type } returns toCfgEnumeratorType(enumerator.type)
    every { it.state } returns toCfgObjectState(enumerator.state)
    every { it.userProperties } returns mockKeyValueCollection()
}
