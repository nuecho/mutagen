package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgSwitchType.CFGFujitsu
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPhysicalSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgSwitchType
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private val physicalSwitch = PhysicalSwitch(
    name = "foo",
    type = CFGFujitsu.toShortName(),
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class PhysicalSwitchTest : ConfigurationObjectTest(
    configurationObject = physicalSwitch,
    emptyConfigurationObject = PhysicalSwitch("foo"),
    mandatoryProperties = setOf(TYPE),
    importedConfigurationObject = PhysicalSwitch(mockPhysicalSwitch())
) {
    val service = mockConfService()

    @Test
    fun `createCfgObject should properly create CfgPhysicalSwitch`() {
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgPhysicalSwitch = physicalSwitch.createCfgObject(service)

            with(cfgPhysicalSwitch) {
                assertThat(name, equalTo(physicalSwitch.name))
                assertThat(type, equalTo(toCfgSwitchType(physicalSwitch.type)))
                assertThat(state, equalTo(toCfgObjectState(physicalSwitch.state)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(physicalSwitch.userProperties))
            }
        }
    }
}

private fun mockPhysicalSwitch() = mockCfgPhysicalSwitch(physicalSwitch.name).apply {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    every { configurationService } returns service
    every { type } returns toCfgSwitchType(physicalSwitch.type)
    every { state } returns toCfgObjectState(physicalSwitch.state)
    every { userProperties } returns mockKeyValueCollection()
    every { folderId } returns DEFAULT_OBJECT_DBID
}
