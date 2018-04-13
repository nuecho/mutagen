package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgSwitchType.CFGFujitsu
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPhysicalSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgSwitchType
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.retrievePhysicalSwitch
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every
import io.mockk.staticMockk
import io.mockk.use

private val physicalSwitch = PhysicalSwitch(
    name = "foo",
    type = CFGFujitsu.toShortName(),
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties()
)

class PhysicalSwitchTest : ConfigurationObjectTest(
    physicalSwitch,
    PhysicalSwitch("foo"),
    PhysicalSwitch(mockPhysicalSwitch())
) {
    init {
        val service = mockConfService()

        "PhysicalSwitch.updateCfgObject should properly create CfgPhysicalSwitch" {
            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
                every { service.retrievePhysicalSwitch(any()) } returns null

                val (status, cfgObject) = physicalSwitch.updateCfgObject(service)
                val cfgPhysicalSwitch = cfgObject as CfgPhysicalSwitch

                status shouldBe CREATED

                with(cfgPhysicalSwitch) {
                    name shouldBe physicalSwitch.name
                    type shouldBe toCfgSwitchType(physicalSwitch.type)
                    state shouldBe toCfgObjectState(physicalSwitch.state)
                    userProperties.size shouldBe 4
                }
            }
        }
    }
}

private fun mockPhysicalSwitch() = mockCfgPhysicalSwitch(physicalSwitch.name).also {
    every { it.type } returns toCfgSwitchType(physicalSwitch.type)
    every { it.state } returns toCfgObjectState(physicalSwitch.state)
    every { it.userProperties } returns mockKeyValueCollection()
}
