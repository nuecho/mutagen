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

import com.genesyslab.platform.applicationblocks.com.objects.CfgGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlaceGroup
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGChat
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGEmail
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.nuecho.genesys.cli.TestResources
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_NAME
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDN
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgObjectiveTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPerson
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgStatTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgDNType
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.StatTableReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveObjectiveTable
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveScript
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.mockk
import io.mockk.staticMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val NAME = "groupName"
private const val INITIALIZED_GROUP_EXPECTED_FILE = "group"
private const val EMPTY_GROUP_EXPECTED_FILE = "empty_group"

private val group = Group(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    managers = listOf(
        PersonReference("employee1", DEFAULT_TENANT_REFERENCE),
        PersonReference("employee2", DEFAULT_TENANT_REFERENCE)
    ),
    routeDNs = listOf(
        DNReference("number1", "switch1", CFGChat),
        DNReference("number2", "switch1", CFGEmail)
    ),
    capacityTable = StatTableReference("capacityTable", DEFAULT_TENANT_REFERENCE),
    quotaTable = StatTableReference("quotaTable", DEFAULT_TENANT_REFERENCE),
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = ConfigurationTestData.defaultProperties(),
    capacityRule = ScriptReference("capacityRule", DEFAULT_TENANT_REFERENCE),
    site = DEFAULT_FOLDER_REFERENCE,
    contract = ObjectiveTableReference("contract", DEFAULT_TENANT_REFERENCE)
)

class GroupTest {
    @Test
    fun `getReferences() should return all references`() {
        val expected = referenceSetBuilder()
            .add(group.tenant)
            .add(group.managers)
            .add(group.routeDNs)
            .add(group.capacityTable)
            .add(group.quotaTable)
            .add(group.capacityRule)
            .add(group.site)
            .add(group.contract)
            .toSet()

        assertThat(group.getReferences(), equalTo(expected))
    }

    @Test
    fun `empty object should properly serialize`() {
        checkSerialization(Group(DEFAULT_TENANT_REFERENCE, "empty-group"), EMPTY_GROUP_EXPECTED_FILE)
    }

    @Test
    fun `initialized object should properly serialize`() {
        checkSerialization(Group(mockGroup()), INITIALIZED_GROUP_EXPECTED_FILE)
    }

    @Test
    fun `fully initialized object should properly serialize`() {
        checkSerialization(group, INITIALIZED_GROUP_EXPECTED_FILE)
    }

    @Test
    fun `object should properly deserialize`() {
        val deserializedConfigurationObject = TestResources.loadJsonConfiguration(
            "models/configuration/group.json",
            Group::class.java
        )
        checkSerialization(deserializedConfigurationObject, INITIALIZED_GROUP_EXPECTED_FILE)
    }

    @Test
    fun `toUpdatedCfgGroup should properly update CfgGroup instance`() {
        val service = mockConfService()
        val parentObject = CfgPlaceGroup(service)

        val capacityTableDbid = 111
        val quotaTableDbid = 112
        val capacityRuleDbid = 113
        val siteDbid = 114
        val contractDbid = 115
        val manager1Dbid = 116
        val manager2Dbid = 117
        val dn1Dbid = 118
        val dn2Dbid = 119

        staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
            mockRetrieveTenant(service)
            mockRetrieveObjectiveTable(service, contractDbid)
            mockRetrieveScript(service, capacityRuleDbid)
            every { service.getObjectDbid(group.capacityTable) } returns capacityTableDbid
            every { service.getObjectDbid(group.quotaTable) } returns quotaTableDbid
            every { service.getObjectDbid(group.site) } returns siteDbid
            every { service.getObjectDbid(group.managers!![0]) } returns manager1Dbid
            every { service.getObjectDbid(group.managers!![1]) } returns manager2Dbid
            every { service.getObjectDbid(group.routeDNs!![0]) } returns dn1Dbid
            every { service.getObjectDbid(group.routeDNs!![1]) } returns dn2Dbid

            val cfgGroup = group.toUpdatedCfgGroup(service, CfgGroup(service, parentObject))

            with(cfgGroup) {
                assertThat(name, equalTo(group.name))
                assertThat(tenantDBID, equalTo(DEFAULT_TENANT_DBID))
                assertThat(capacityTableDBID, equalTo(capacityTableDbid))
                assertThat(quotaTableDBID, equalTo(quotaTableDbid))
                assertThat(capacityRuleDBID, equalTo(capacityRuleDbid))
                assertThat(siteDBID, equalTo(siteDbid))
                assertThat(contractDBID, equalTo(contractDbid))

                assertThat(managerDBIDs.toList(), equalTo(listOf(manager1Dbid, manager2Dbid)))
                assertThat(routeDNDBIDs.toList(), equalTo(listOf(dn1Dbid, dn2Dbid)))

                assertThat(state, equalTo(ConfigurationObjects.toCfgObjectState(group.state)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(group.userProperties))
            }
        }
    }
}

private fun mockGroup(): CfgGroup {
    val tenant = mockCfgTenant(DEFAULT_TENANT_NAME)

    val cfgGroup = mockk<CfgGroup>().also {
        every { it.name } returns group.name
        every { it.tenant } returns tenant
    }

    val managersMock = group.managers?.map { ref -> mockCfgPerson(ref.primaryKey) }

    val routeDNsMock = group.routeDNs?.map { ref ->
        val cfgSwitch = mockCfgSwitch(ref.switch.primaryKey)
        mockCfgDN(ref.number, toCfgDNType(ref.type)!!).apply {
            every { switch } returns cfgSwitch
            every { name } returns null
        }
    }
    val capacityTableMock = mockCfgStatTable(group.capacityTable!!.primaryKey)
    val quotaTableMock = mockCfgStatTable(group.quotaTable!!.primaryKey)
    val capacityRuleMock = mockCfgScript(group.capacityRule!!.primaryKey)
    val siteMock = mockCfgFolder(DEFAULT_FOLDER, CfgObjectType.CFGFolder)
    val contractMock = mockCfgObjectiveTable(group.contract!!.primaryKey)

    return cfgGroup.apply {
        every { managers } returns managersMock
        every { routeDNs } returns routeDNsMock
        every { capacityTable } returns capacityTableMock
        every { quotaTable } returns quotaTableMock
        every { userProperties } returns mockKeyValueCollection()
        every { capacityRule } returns capacityRuleMock
        every { site } returns siteMock
        every { state } returns CfgObjectState.CFGEnabled
        every { contract } returns contractMock
    }
}
