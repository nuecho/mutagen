package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.configuration.protocol.types.CfgLinkType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgRouteType
import com.genesyslab.platform.configuration.protocol.types.CfgTargetType
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
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveApplication
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrievePhysicalSwitch
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.spyk
import io.mockk.staticMockk
import io.mockk.use
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val MAIN_SWITCH = "main-switch"
private const val OTHER_SWITCH = "other-switch"
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
    )
)

class SwitchTest : ConfigurationObjectTest(
    mainSwitch,
    Switch(tenant = DEFAULT_TENANT_REFERENCE, name = MAIN_SWITCH),
    Switch(mockMainCfgSwitch())
) {
    @Test
    fun `updateCfgObject should properly create CfgSwitch`() {
        staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
            val otherCfgSwitch = mockOtherCfgSwitch()

            val service = mockConfService()
            every { service.retrieveObject(SwitchReference(MAIN_SWITCH, DEFAULT_TENANT_REFERENCE)) } returns null
            every { service.retrieveObject(SwitchReference(OTHER_SWITCH, DEFAULT_TENANT_REFERENCE)) } returns otherCfgSwitch
            mockRetrieveTenant(service)
            mockRetrievePhysicalSwitch(service)
            mockRetrieveApplication(service)

            val cfgSwitch = mainSwitch.updateCfgObject(service)

            with(cfgSwitch) {
                assertEquals(mainSwitch.name, name)
                assertEquals(DEFAULT_OBJECT_DBID, physSwitchDBID)
                assertEquals(DEFAULT_OBJECT_DBID, tServerDBID)
                assertEquals(CfgLinkType.CFGMadgeLink, linkType)
                assertEquals(mainSwitch.dnRange, dnRange)
                assertEquals(toCfgObjectState(mainSwitch.state), state)
                assertEquals(mainSwitch.userProperties, userProperties.asCategorizedProperties())

                assertEquals(2, switchAccessCodes.size)

                switchAccessCodes.zip(mainSwitch.switchAccessCodes!!) { actual, expected ->
                    with(actual) {
                        assertEquals(expected.accessCode, accessCode)
                        assertEquals(toCfgTargetType(expected.targetType), targetType)
                        assertEquals(toCfgRouteType(expected.routeType), routeType)
                        assertEquals(expected.dnSource, dnSource)
                        assertEquals(expected.destinationSource, destinationSource)
                        assertEquals(expected.locationSource, locationSource)
                        assertEquals(expected.dnisSource, dnisSource)
                        assertEquals(expected.reasonSource, reasonSource)
                        assertEquals(expected.extensionSource, extensionSource)
                    }
                }

                assertEquals(103, switchAccessCodes.elementAt(0).switchDBID)
                assertEquals(0, switchAccessCodes.elementAt(1).switchDBID)
            }
        }
    }
}

private fun mockMainCfgSwitch(): CfgSwitch {
    val service = mockConfService()
    staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
        val otherCfgSwitch = mockOtherCfgSwitch()
        every {
            service.retrieveObject(SwitchReference("other-switch", DEFAULT_TENANT_REFERENCE))
        } returns otherCfgSwitch

        val mainCfgSwitch = mockCfgSwitch(mainSwitch.name)
        val accessCodes = mainSwitch.switchAccessCodes?.map { accessCode ->
            spyk(accessCode.toCfgSwitchAccessCode(service, mainCfgSwitch)).apply {
                every { switch } returns otherCfgSwitch
            }
        }

        val tServerApplication = mockCfgApplication("tServer")
        val physicalSwitch = mockCfgPhysicalSwitch("physicalSwitch")
        val cfgLinkType = toCfgLinkType(mainSwitch.linkType)
        val objectState = toCfgObjectState(mainSwitch.state)

        return mainCfgSwitch.apply {
            every { linkType } returns cfgLinkType
            every { dnRange } returns mainSwitch.dnRange
            every { switchAccessCodes } returns accessCodes
            every { state } returns objectState
            every { userProperties } returns mockKeyValueCollection()
            every { physSwitch } returns physicalSwitch
            every { tServer } returns tServerApplication
        }
    }
}

private fun mockOtherCfgSwitch() = mockCfgSwitch(OTHER_SWITCH).apply {
    every { dbid } returns 103
    every { objectDbid } returns 103
}
