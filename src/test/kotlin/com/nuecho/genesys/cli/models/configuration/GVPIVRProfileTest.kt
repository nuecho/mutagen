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

package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPIVRProfile
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGACDQueue
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_IVR_PROFILE_TYPE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDN
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgGVPIVRProfile
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgDNType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgIVRProfileType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveDN
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveSwitch
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.GregorianCalendar

private const val NAME = "name"
private const val SWITCH_NAME = "switch1"
private val SIMPLE_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
private val DN = DNReference("number1", SWITCH_NAME, CFGACDQueue, "dnName1", DEFAULT_TENANT_REFERENCE)

private val gvpIVRProfile = GVPIVRProfile(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    displayName = NAME,
    description = "a description",
    notes = "a note",
    dids = listOf(DN),
    type = DEFAULT_IVR_PROFILE_TYPE.toShortName(),
    startServiceDate = SIMPLE_DATE_FORMAT.parse("2018-06-06T21:17:50.105+0000"),
    endServiceDate = SIMPLE_DATE_FORMAT.parse("2019-06-06T21:17:50.105+0000"),
    tfn = listOf("1888", "1877"),
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class GVPIVRProfileTest : ConfigurationObjectTest(
    configurationObject = gvpIVRProfile,
    emptyConfigurationObject = GVPIVRProfile(name = NAME),
    mandatoryProperties = setOf(DISPLAY_NAME, TENANT),
    importedConfigurationObject = GVPIVRProfile(mockCfgGVPIVRProfile())
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(gvpIVRProfile.tenant)
            .add(gvpIVRProfile.dids)
            .add(gvpIVRProfile.folder)
            .toSet()

        assertThat(gvpIVRProfile.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        assertUnchangeableProperties(
            mockCfgGVPIVRProfile(gvpIVRProfile.name, mockCfgTenant("differentTenantName")), FOLDER, TENANT
        )

    @Test
    fun `createCfgObject should properly create CfgGVPIVRProfile`() {
        val service = mockConfService()
        val dnDbid = 102

        every { service.retrieveObject(CfgGVPIVRProfile::class.java, any()) } returns null
        mockRetrieveTenant(service)
        mockRetrieveDN(service, mockCfgSwitch(SWITCH_NAME), dnDbid)
        mockRetrieveSwitch(service)

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgGVPIVRProfile = gvpIVRProfile.createCfgObject(service)

            with(cfgGVPIVRProfile) {
                assertThat(description, equalTo(gvpIVRProfile.description))
                assertThat(diddbiDs.toList(), equalTo(listOf(dnDbid)))
                assertThat(displayName, equalTo(gvpIVRProfile.displayName))
                assertThat(endServiceDate.time, equalTo(gvpIVRProfile.endServiceDate))
                assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
                assertThat(name, equalTo(gvpIVRProfile.name))
                assertThat(notes, equalTo(gvpIVRProfile.notes))
                assertThat(startServiceDate.time, equalTo(gvpIVRProfile.startServiceDate))
                assertThat(state, equalTo(toCfgObjectState(gvpIVRProfile.state)))
                assertThat(tenantDBID, equalTo(DEFAULT_TENANT_DBID))
                assertThat(tfn.split(',').map { it.trim() }, equalTo(gvpIVRProfile.tfn))
                assertThat(type, equalTo(toCfgIVRProfileType(gvpIVRProfile.type)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(gvpIVRProfile.userProperties))
            }
        }
    }
}

private fun mockCfgGVPIVRProfile() = mockCfgGVPIVRProfile(gvpIVRProfile.name).apply {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    every { configurationService } returns service
    every { customer } returns null
    every { reseller } returns null

    every { displayName } returns gvpIVRProfile.displayName
    every { type } returns DEFAULT_IVR_PROFILE_TYPE
    every { notes } returns gvpIVRProfile.notes
    every { description } returns gvpIVRProfile.description

    every { startServiceDate } returns GregorianCalendar.from(
        ZonedDateTime.ofInstant(gvpIVRProfile.startServiceDate!!.toInstant(), ZoneId.systemDefault())
    )
    every { endServiceDate } returns GregorianCalendar.from(
        ZonedDateTime.ofInstant(gvpIVRProfile.endServiceDate!!.toInstant(), ZoneId.systemDefault())
    )

    every { isProvisioned } returns toCfgFlag(gvpIVRProfile.isProvisioned)

    every { tfn } returns gvpIVRProfile.tfn?.joinToString()
    every { status } returns gvpIVRProfile.status

    val switch = mockCfgSwitch(SWITCH_NAME)
    val did = mockCfgDN(number = DN.number, type = toCfgDNType(DN.type)!!, switch = switch).apply {
        every { name } returns DN.name
    }
    every { diDs } returns listOf(did)

    every { state } returns toCfgObjectState(gvpIVRProfile.state)
    every { userProperties } returns mockKeyValueCollection()
    every { folderId } returns DEFAULT_FOLDER_DBID
}
