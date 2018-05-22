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
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
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
import io.kotlintest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import io.mockk.staticMockk
import io.mockk.use

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
            switch = SwitchReference(OTHER_SWITCH),
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
    init {
        "Switch.updateCfgObject should properly create CfgSwitch" {
            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
                val otherCfgSwitch = mockOtherCfgSwitch()

                val service = mockConfService()
                every { service.retrieveObject(SwitchReference(MAIN_SWITCH)) } returns null
                every { service.retrieveObject(SwitchReference(OTHER_SWITCH)) } returns otherCfgSwitch
                mockRetrieveTenant(service)
                mockRetrievePhysicalSwitch(service)
                mockRetrieveApplication(service)

                val (status, cfgObject) = mainSwitch.updateCfgObject(service)
                val cfgSwitch = cfgObject as CfgSwitch

                status shouldBe CREATED

                with(cfgSwitch) {
                    name shouldBe mainSwitch.name
                    physSwitchDBID shouldBe DEFAULT_OBJECT_DBID
                    tServerDBID shouldBe DEFAULT_OBJECT_DBID
                    linkType shouldBe CfgLinkType.CFGMadgeLink
                    dnRange shouldBe mainSwitch.dnRange
                    state shouldBe toCfgObjectState(mainSwitch.state)
                    userProperties.asCategorizedProperties() shouldBe mainSwitch.userProperties

                    switchAccessCodes.size shouldBe 2

                    switchAccessCodes.zip(mainSwitch.switchAccessCodes!!) { actual, expected ->
                        with(actual) {
                            accessCode shouldBe expected.accessCode
                            targetType shouldBe toCfgTargetType(expected.targetType)
                            routeType shouldBe toCfgRouteType(expected.routeType)
                            dnSource shouldBe expected.dnSource
                            destinationSource shouldBe expected.destinationSource
                            locationSource shouldBe expected.locationSource
                            dnisSource shouldBe expected.dnisSource
                            reasonSource shouldBe expected.reasonSource
                            extensionSource shouldBe expected.extensionSource
                        }
                    }

                    switchAccessCodes.elementAt(0).switchDBID shouldBe 103
                    switchAccessCodes.elementAt(1).switchDBID shouldBe 0
                }
            }
        }
    }
}

private fun mockMainCfgSwitch(): CfgSwitch {
    val service = mockConfService()
    staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
        val otherCfgSwitch = mockOtherCfgSwitch()
        every {
            service.retrieveObject(SwitchReference("other-switch"))
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
