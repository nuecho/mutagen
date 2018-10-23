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

import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPCustomer
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgGVPCustomer
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgGVPReseller
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTimeZone
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.GVPResellerReference
import com.nuecho.genesys.cli.models.configuration.reference.TimeZoneReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveReseller
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTimeZone
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val NAME = "name"
private val gvpCustomer = GVPCustomer(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    displayName = "DisplayName",
    channel = "achannel",
    isProvisioned = false,
    isAdminCustomer = false,
    notes = "some notes",
    reseller = GVPResellerReference("areseller", DEFAULT_TENANT_REFERENCE),
    timeZone = TimeZoneReference("timeZone1", DEFAULT_TENANT_REFERENCE),
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class GVPCustomerTest : ConfigurationObjectTest(
    configurationObject = gvpCustomer,
    emptyConfigurationObject = GVPCustomer(name = NAME),
    mandatoryProperties = setOf(CHANNEL, IS_ADMIN_CUSTOMER, IS_PROVISIONED, RESELLER, TENANT),
    importedConfigurationObject = GVPCustomer(mockCfgGVPCustomer())
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(gvpCustomer.tenant)
            .add(gvpCustomer.reseller)
            .add(gvpCustomer.folder)
            .add(gvpCustomer.timeZone)
            .toSet()

        assertThat(gvpCustomer.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        mockCfgGVPCustomer(
            name = gvpCustomer.name,
            tenant = mockCfgTenant("differentTenantName")
        ).let {
            every { it.reseller } returns null
            assertUnchangeableProperties(it, FOLDER, RESELLER, TENANT)
        }

    @Test
    fun `createCfgObject should properly create CfgGVPCustomer`() {
        val service = mockConfService()
        val resellerDbid = 102
        val timeZoneDbid = 103

        every { service.retrieveObject(CfgGVPCustomer::class.java, any()) } returns null
        mockRetrieveTenant(service)
        mockRetrieveReseller(service, resellerDbid)
        mockRetrieveTimeZone(service, timeZoneDbid)

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgGVPCustomer = gvpCustomer.createCfgObject(service)

            with(cfgGVPCustomer) {
                assertThat(name, equalTo(gvpCustomer.name))
                assertThat(displayName, equalTo(gvpCustomer.displayName))
                assertThat(channel, equalTo(gvpCustomer.channel))
                assertThat(isProvisioned, equalTo(toCfgFlag(gvpCustomer.isProvisioned)))
                assertThat(isAdminCustomer, equalTo(toCfgFlag(gvpCustomer.isAdminCustomer)))
                assertThat(notes, equalTo(gvpCustomer.notes))
                assertThat(resellerDBID, equalTo(resellerDbid))
                assertThat(timeZoneDBID, equalTo(timeZoneDbid))
                assertThat(state, equalTo(toCfgObjectState(gvpCustomer.state)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(gvpCustomer.userProperties))
                assertThat(folderId, equalTo(ConfigurationObjectMocks.DEFAULT_FOLDER_DBID))
            }
        }
    }
}

private fun mockCfgGVPCustomer() = mockCfgGVPCustomer(gvpCustomer.name).apply {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    val resellerMock = mockCfgGVPReseller(gvpCustomer.reseller!!.primaryKey)
    val timezone = mockCfgTimeZone("timeZone1")

    every { configurationService } returns service
    every { state } returns toCfgObjectState(gvpCustomer.state)
    every { channel } returns gvpCustomer.channel
    every { reseller } returns resellerMock

    every { notes } returns gvpCustomer.notes
    every { displayName } returns gvpCustomer.displayName
    every { isAdminCustomer } returns toCfgFlag(gvpCustomer.isAdminCustomer)
    every { isProvisioned } returns toCfgFlag(gvpCustomer.isProvisioned)
    every { notes } returns gvpCustomer.notes
    every { timeZone } returns timezone

    every { userProperties } returns mockKeyValueCollection()
    every { folderId } returns DEFAULT_FOLDER_DBID
}
