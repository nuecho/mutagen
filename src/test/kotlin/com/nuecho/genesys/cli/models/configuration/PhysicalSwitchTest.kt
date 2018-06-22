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
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
    val service = mockConfService()

    @Test
    fun `updateCfgObject should properly create CfgPhysicalSwitch`() {
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

        val cfgPhysicalSwitch = physicalSwitch.updateCfgObject(service)

        with(cfgPhysicalSwitch) {
            assertEquals(physicalSwitch.name, name)
            assertEquals(toCfgSwitchType(physicalSwitch.type), type)
            assertEquals(toCfgObjectState(physicalSwitch.state), state)
            assertEquals(physicalSwitch.userProperties, userProperties.asCategorizedProperties())
        }
    }
}

private fun mockPhysicalSwitch() = mockCfgPhysicalSwitch(physicalSwitch.name).apply {
    every { type } returns toCfgSwitchType(physicalSwitch.type)
    every { state } returns toCfgObjectState(physicalSwitch.state)
    every { userProperties } returns mockKeyValueCollection()
}
