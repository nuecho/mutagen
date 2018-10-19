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

import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGCellular
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDN
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgObjectiveTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPlace
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveObjectiveTable
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveScript
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveSwitch
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

private const val NAME = "name"
private const val DN_DBID_1 = 123
private const val DN_DBID_2 = 456
private val switch = SwitchReference("switch", DEFAULT_TENANT_REFERENCE)
private val dnType = CFGCellular
private val dn1 = DNReference("123", switch, dnType.toShortName(), null, DEFAULT_TENANT_REFERENCE)
private val dn2 = DNReference("456", switch, dnType.toShortName(), null, DEFAULT_TENANT_REFERENCE)

private val place = Place(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    dns = listOf(dn1, dn2),
    capacityRule = ScriptReference("capacityRule", DEFAULT_TENANT_REFERENCE),
    contract = ObjectiveTableReference("contract", DEFAULT_TENANT_REFERENCE),
    site = DEFAULT_FOLDER_REFERENCE,
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class PlaceTest : ConfigurationObjectTest(
    configurationObject = place,
    emptyConfigurationObject = Place(tenant = DEFAULT_TENANT_REFERENCE, name = NAME),
    mandatoryProperties = emptySet(),
    importedConfigurationObject = Place(mockCfgPlace())
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(place.tenant)
            .add(place.dns)
            .add(place.capacityRule)
            .add(place.contract)
            .add(place.site)
            .add(place.folder)
            .toSet()

        assertThat(place.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        assertUnchangeableProperties(mockCfgPlace(), FOLDER)

    @Test
    fun `updateCfgObject should properly create CfgPlace`() {
        val contractDbid = 222
        val capacityRuleDbid = 333

        val service = mockConfService()
        every { service.retrieveObject(CfgPlace::class.java, any()) } returns null
        mockRetrieveTenant(service)
        mockRetrieveSwitch(service)
        mockRetrieveObjectiveTable(service, contractDbid)
        mockRetrieveScript(service, capacityRuleDbid)

        val dn1Mock = mockCfgDN(dn1.number, DN_DBID_1)
        val dn2Mock = mockCfgDN(dn2.number, DN_DBID_2)
        every { service.retrieveObject(CfgDN::class.java, any()) } returns dn1Mock andThen dn2Mock

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()

            val cfgPlace = place.createCfgObject(service)

            with(cfgPlace) {
                assertThat(name, equalTo(place.name))
                assertThat(tenantDBID, equalTo(DEFAULT_TENANT_DBID))
                assertThat(dndbiDs, equalTo(listOf(DN_DBID_1, DN_DBID_2) as Collection<Int>))
                assertThat(siteDBID, equalTo(DEFAULT_FOLDER_DBID))
                assertThat(contractDBID, equalTo(contractDbid))
                assertThat(capacityRuleDBID, equalTo(capacityRuleDbid))
                assertThat(state, equalTo(toCfgObjectState(place.state)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(place.userProperties))
                assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
            }
        }
    }
}

private fun mockCfgPlace() = mockCfgPlace(place.name).apply {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    val dn1Mock = mockCfgDN(dn1.number, DN_DBID_1)
    val dn2Mock = mockCfgDN(dn2.number, DN_DBID_2)
    val capacityRuleMock = mockCfgScript("capacityRule")
    val contractMock = mockCfgObjectiveTable("contract")
    val siteMock = mockCfgFolder()

    every { configurationService } returns service
    every { dNs } returns listOf(dn1Mock, dn2Mock)
    every { capacityRule } returns capacityRuleMock
    every { contract } returns contractMock
    every { site } returns siteMock
    every { state } returns ConfigurationObjects.toCfgObjectState(place.state)
    every { userProperties } returns mockKeyValueCollection()
    every { folderId } returns DEFAULT_FOLDER_DBID
}

private fun mockCfgDN(number: String, dbid: Int) = mockCfgDN(
    number = number,
    dbid = dbid
).also {
    val switch = mockCfgSwitch(switch.primaryKey)
    every { it.switch } returns switch
    every { it.type } returns dnType
    every { it.name } returns null
}
