package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.configuration.protocol.types.CfgLinkType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgRouteType
import com.genesyslab.platform.configuration.protocol.types.CfgTargetType
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
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.retrieveSwitch
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import io.mockk.staticMockk
import io.mockk.use

private val mainSwitch = Switch(
    name = "main-switch",
    physicalSwitch = "physicalSwitch",
    tServer = "tServer",
    linkType = CfgLinkType.CFGMadgeLink.toShortName(),
    dnRange = "74233-74244, 74290",
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    switchAccessCodes = listOf(
        SwitchAccessCode(
            switch = "other-switch",
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
            switch = "",
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

class SwitchTest : ConfigurationObjectTest(mainSwitch, Switch("switch"), Switch(mockMainCfgSwitch())) {
    init {
        val service = mockConfService()

        "Switch.updateCfgObject should properly create CfgSwitch" {
            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
                val otherCfgSwitch = mockOtherCfgSwitch()
                every { service.retrieveSwitch("main-switch") } returns null
                every { service.retrieveSwitch("other-switch") } returns otherCfgSwitch

                ConfServiceExtensionMocks.mockRetrievePhysicalSwitch(service, 101)
                ConfServiceExtensionMocks.mockRetrieveApplication(service, 102)

                val (status, cfgObject) = mainSwitch.updateCfgObject(service)
                val cfgSwitch = cfgObject as CfgSwitch

                status shouldBe CREATED

                with(cfgSwitch) {
                    name shouldBe mainSwitch.name
                    physSwitchDBID shouldBe 101
                    tServerDBID shouldBe 102
                    linkType shouldBe CfgLinkType.CFGMadgeLink
                    dnRange shouldBe mainSwitch.dnRange
                    state shouldBe toCfgObjectState(mainSwitch.state)
                    userProperties.size shouldBe 4
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
        every { service.retrieveSwitch("other-switch") } returns otherCfgSwitch

        val mainCfgSwitch = mockCfgSwitch(mainSwitch.name)
        val accessCodes = mainSwitch.switchAccessCodes?.map { accessCode ->
            spyk(accessCode.toCfgSwitchAccessCode(service, mainCfgSwitch)).apply {
                every { switch } returns otherCfgSwitch
            }
        }

        val tServer = mockCfgApplication("tServer")
        val physicalSwitch = mockCfgPhysicalSwitch("physicalSwitch")
        val linkType = toCfgLinkType(mainSwitch.linkType)
        val objectState = toCfgObjectState(mainSwitch.state)
        val userProperties = mockKeyValueCollection()

        return mainCfgSwitch.also {
            every { it.linkType } returns linkType
            every { it.dnRange } returns mainSwitch.dnRange
            every { it.switchAccessCodes } returns accessCodes
            every { it.state } returns objectState
            every { it.userProperties } returns userProperties
            every { it.physSwitch } returns physicalSwitch
            every { it.tServer } returns tServer
        }
    }
}

private fun mockOtherCfgSwitch() = mockCfgSwitch("other-switch").also {
    every { it.dbid } returns 103
    every { it.objectDbid } returns 103
}
