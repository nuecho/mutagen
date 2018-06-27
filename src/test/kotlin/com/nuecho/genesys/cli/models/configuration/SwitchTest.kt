package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitchAccessCode
import com.genesyslab.platform.configuration.protocol.types.CfgLinkType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgRouteType
import com.genesyslab.platform.configuration.protocol.types.CfgTargetType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgApplication
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPhysicalSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgLinkType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgRouteType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgTargetType
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.ApplicationReference
import com.nuecho.genesys.cli.models.configuration.reference.PhysicalSwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveApplication
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrievePhysicalSwitch
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.mockk
import io.mockk.objectMockk
import io.mockk.staticMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test

private const val MAIN_SWITCH = "main-switch"
private const val OTHER_SWITCH = "other-switch"
private const val OTHER_SWITCH_DBID = 103

private val mainSwitch = Switch(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = MAIN_SWITCH,
    physicalSwitch = PhysicalSwitchReference("physicalSwitch"),
    tServer = ApplicationReference("tServer"),
    linkType = CfgLinkType.CFGMadgeLink.toShortName(),
    dnRange = "74233-74244, 74290",
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    switchAccessCodes = listOf(
        SwitchAccessCode(
            switch = SwitchReference(OTHER_SWITCH, DEFAULT_TENANT_REFERENCE),
            accessCode = "123",
            targetType = CfgTargetType.CFGMaxTargetType.toShortName(),
            routeType = CfgRouteType.CFGAnnouncement.toShortName(),
            dnSource = "dnSource1",
            destinationSource = "destinationSource1",
            locationSource = "locationSource1",
            dnisSource = "dnisSource1",
            reasonSource = "reasonSource1",
            extensionSource = "extensionSource1"
        ),
        SwitchAccessCode(
            switch = null,
            accessCode = "234",
            targetType = CfgTargetType.CFGTargetAgentGroup.toShortName(),
            routeType = CfgRouteType.CFGIDDD.toShortName(),
            dnSource = "dnSource2",
            destinationSource = "destinationSource2",
            locationSource = "locationSource2",
            dnisSource = "dnisSource2",
            reasonSource = "reasonSource2",
            extensionSource = "extensionSource2"
        )
    ),
    folder = DEFAULT_FOLDER_REFERENCE
)

class SwitchTest : ConfigurationObjectTest(
    mainSwitch,
    Switch(tenant = DEFAULT_TENANT_REFERENCE, name = MAIN_SWITCH),
    setOf(PHYSICAL_SWITCH),
    Switch(mockMainCfgSwitch())
) {
    @Test
    fun `updateCfgObject should properly create CfgSwitch`() {
        val service = mockConfService()
        mockRetrieveTenant(service)
        mockRetrievePhysicalSwitch(service)
        mockRetrieveApplication(service)

        staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
            val otherCfgSwitch = mockOtherCfgSwitch()
            every { service.retrieveObject(SwitchReference(MAIN_SWITCH, DEFAULT_TENANT_REFERENCE)) } returns null
            every { service.retrieveObject(SwitchReference(OTHER_SWITCH, DEFAULT_TENANT_REFERENCE)) } returns otherCfgSwitch

            objectMockk(ConfigurationObjectRepository).use {
                mockConfigurationObjectRepository()
                val cfgSwitch = mainSwitch.updateCfgObject(service)

                with(cfgSwitch) {
                    assertThat(name, equalTo(mainSwitch.name))
                    assertThat(physSwitchDBID, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(tServerDBID, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(linkType, equalTo(CfgLinkType.CFGMadgeLink))
                    assertThat(dnRange, equalTo(mainSwitch.dnRange))
                    assertThat(state, equalTo(toCfgObjectState(mainSwitch.state)))
                    assertThat(userProperties.asCategorizedProperties(), equalTo(mainSwitch.userProperties))

                    assertThat(switchAccessCodes, hasSize(2))

                    switchAccessCodes.zip(mainSwitch.switchAccessCodes!!) { actual, expected ->
                        with(actual) {
                            assertThat(accessCode, equalTo(expected.accessCode))
                            assertThat(targetType, equalTo(toCfgTargetType(expected.targetType)))
                            assertThat(routeType, equalTo(toCfgRouteType(expected.routeType)))
                            assertThat(dnSource, equalTo(expected.dnSource))
                            assertThat(destinationSource, equalTo(expected.destinationSource))
                            assertThat(locationSource, equalTo(expected.locationSource))
                            assertThat(dnisSource, equalTo(expected.dnisSource))
                            assertThat(reasonSource, equalTo(expected.reasonSource))
                            assertThat(extensionSource, equalTo(expected.extensionSource))
                        }
                    }

                    assertThat(switchAccessCodes.elementAt(0).switchDBID, equalTo(OTHER_SWITCH_DBID))
                    assertThat(switchAccessCodes.elementAt(1).switchDBID, equalTo(0))
                }
            }
        }
    }
}

private fun mockMainCfgSwitch(): CfgSwitch {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
        val otherCfgSwitch = mockOtherCfgSwitch()
        every {
            service.retrieveObject(SwitchReference("other-switch", DEFAULT_TENANT_REFERENCE))
        } returns otherCfgSwitch

        val mainCfgSwitch = mockCfgSwitch(mainSwitch.name)
        val accessCodes = mainSwitch.switchAccessCodes?.map { code ->
            mockk<CfgSwitchAccessCode>().apply {
                every { accessCode } returns code.accessCode
                every { targetType } returns toCfgTargetType(code.targetType)
                every { routeType } returns toCfgRouteType(code.routeType)
                every { dnSource } returns code.dnSource
                every { destinationSource } returns code.destinationSource
                every { locationSource } returns code.locationSource
                every { dnisSource } returns code.dnisSource
                every { reasonSource } returns code.reasonSource
                every { extensionSource } returns code.extensionSource

                every { switchDBID } returns if (code.switch == null) 0 else OTHER_SWITCH_DBID
                every { switch } returns if (code.switch == null) null else otherCfgSwitch
            }
        }

        val tServerApplication = mockCfgApplication("tServer")
        val physicalSwitch = mockCfgPhysicalSwitch("physicalSwitch")
        val cfgLinkType = toCfgLinkType(mainSwitch.linkType)
        val objectState = toCfgObjectState(mainSwitch.state)

        return mainCfgSwitch.apply {
            every { configurationService } returns service
            every { linkType } returns cfgLinkType
            every { dnRange } returns mainSwitch.dnRange
            every { switchAccessCodes } returns accessCodes
            every { state } returns objectState
            every { userProperties } returns mockKeyValueCollection()
            every { physSwitch } returns physicalSwitch
            every { tServer } returns tServerApplication
            every { folderId } returns DEFAULT_OBJECT_DBID
        }
    }
}

private fun mockOtherCfgSwitch() = mockCfgSwitch(OTHER_SWITCH).apply {
    every { dbid } returns OTHER_SWITCH_DBID
    every { objectDbid } returns OTHER_SWITCH_DBID
}
