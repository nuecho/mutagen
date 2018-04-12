package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.configuration.protocol.types.CfgEnumeratorType.CFGENTRole
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgEnumeratorType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.retrieveEnumerator
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
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

class EnumeratorTest : ConfigurationObjectTest(enumerator, Enumerator(NAME)) {
    init {
        "CfgEnumerator initialized Enumerator should properly serialize" {
            val enumerator = Enumerator(mockCfgEnumerator())
            checkSerialization(enumerator, "enumerator")
        }

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

    private fun mockCfgEnumerator(): CfgEnumerator {
        val cfgEnumerator = mockk<CfgEnumerator>()

        val type = toCfgEnumeratorType(enumerator.type)
        val state = toCfgObjectState(enumerator.state)

        every { cfgEnumerator.name } returns enumerator.name
        every { cfgEnumerator.displayName } returns enumerator.displayName
        every { cfgEnumerator.description } returns enumerator.description
        every { cfgEnumerator.type } returns type
        every { cfgEnumerator.state } returns state
        every { cfgEnumerator.userProperties } returns mockKeyValueCollection()
        every { cfgEnumerator.configurationService } returns service

        return cfgEnumerator
    }
}
