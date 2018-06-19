package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgSwitchType.CFGFujitsu
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPhysicalSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgSwitchType
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every

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
            every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

            val cfgPhysicalSwitch = physicalSwitch.updateCfgObject(service)

            with(cfgPhysicalSwitch) {
                name shouldBe physicalSwitch.name
                type shouldBe toCfgSwitchType(physicalSwitch.type)
                state shouldBe toCfgObjectState(physicalSwitch.state)
                userProperties.asCategorizedProperties() shouldBe physicalSwitch.userProperties
            }
        }
    }
}

private fun mockPhysicalSwitch() = mockCfgPhysicalSwitch(physicalSwitch.name).apply {
    every { type } returns toCfgSwitchType(physicalSwitch.type)
    every { state } returns toCfgObjectState(physicalSwitch.state)
    every { userProperties } returns mockKeyValueCollection()
}
