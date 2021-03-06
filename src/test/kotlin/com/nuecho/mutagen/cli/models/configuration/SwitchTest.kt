/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nuecho.mutagen.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitchAccessCode
import com.genesyslab.platform.configuration.protocol.types.CfgLinkType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgRouteType
import com.genesyslab.platform.configuration.protocol.types.CfgTargetType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgApplication
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgPhysicalSwitch
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgLinkType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgRouteType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgTargetType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.mutagen.cli.models.configuration.reference.ApplicationReference
import com.nuecho.mutagen.cli.models.configuration.reference.PhysicalSwitchReference
import com.nuecho.mutagen.cli.models.configuration.reference.SwitchReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveApplication
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrievePhysicalSwitch
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.mutagen.cli.services.ServiceMocks.mockConfService
import com.nuecho.mutagen.cli.toShortName
import io.mockk.every
import io.mockk.mockk
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
    configurationObject = mainSwitch,
    emptyConfigurationObject = Switch(tenant = DEFAULT_TENANT_REFERENCE, name = MAIN_SWITCH),
    mandatoryProperties = setOf(PHYSICAL_SWITCH),
    importedConfigurationObject = Switch(mockMainCfgSwitch())
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(mainSwitch.tenant)
            .add(mainSwitch.physicalSwitch)
            .add(mainSwitch.tServer)
            .add(mainSwitch.switchAccessCodes!!.mapNotNull { it.switch })
            .add(mainSwitch.folder)
            .toSet()

        assertThat(mainSwitch.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        mockCfgSwitch(name = mainSwitch.name).let {
            val differentPhysicalSwitch = mockCfgPhysicalSwitch(name = "differentPhysicalSwitch")
            every { it.physSwitch } returns differentPhysicalSwitch
            assertUnchangeableProperties(it, FOLDER, PHYSICAL_SWITCH)
        }

    @Test
    fun `createCfgObject should properly create CfgSwitch`() {
        val service = mockConfService()
        val physicalSwitchDbid = 102
        val applicationDbid = 103

        mockRetrieveTenant(service)
        mockRetrievePhysicalSwitch(service, physicalSwitchDbid)
        mockRetrieveApplication(service, applicationDbid)

        val otherCfgSwitch = mockOtherCfgSwitch()
        every { service.retrieveObject(SwitchReference(MAIN_SWITCH, DEFAULT_TENANT_REFERENCE)) } returns null
        every { service.retrieveObject(SwitchReference(OTHER_SWITCH, DEFAULT_TENANT_REFERENCE)) } returns otherCfgSwitch

        val cfgSwitch = mainSwitch.createCfgObject(service)
        with(cfgSwitch) {
            assertThat(name, equalTo(mainSwitch.name))
            assertThat(physSwitchDBID, equalTo(physicalSwitchDbid))
            assertThat(tServerDBID, equalTo(applicationDbid))
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

private fun mockMainCfgSwitch(): CfgSwitch {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

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
        every { folderId } returns DEFAULT_FOLDER_DBID
    }
}

private fun mockOtherCfgSwitch() = mockCfgSwitch(OTHER_SWITCH).apply {
    every { dbid } returns OTHER_SWITCH_DBID
    every { objectDbid } returns OTHER_SWITCH_DBID
}
