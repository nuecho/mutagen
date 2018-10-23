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

import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgObjectiveTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveObjectiveTable
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveScript
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

private val tenant = Tenant(
    name = "foo",
    defaultCapacityRule = ScriptReference("capacityRule", DEFAULT_TENANT_REFERENCE),
    defaultContract = ObjectiveTableReference("contract", DEFAULT_TENANT_REFERENCE),
    chargeableNumber = "123",
    password = "password",
    parentTenant = TenantReference("parent"),
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class TenantTest : ConfigurationObjectTest(
    configurationObject = tenant,
    emptyConfigurationObject = Tenant("foo"),
    mandatoryProperties = emptySet(),
    importedConfigurationObject = Tenant(mockCfgTenant())
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(tenant.parentTenant)
            .add(tenant.defaultCapacityRule)
            .add(tenant.defaultContract)
            .add(tenant.folder)
            .toSet()

        assertThat(tenant.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        assertUnchangeableProperties(mockCfgTenant(), FOLDER)

    @Test
    fun `createCfgObject should properly create CfgTenant`() {
        val service = mockConfService()
        mockRetrieveTenant(service)
        val objectiveTableDbid = 102
        val scriptDbid = 103

        mockRetrieveObjectiveTable(service, objectiveTableDbid)
        mockRetrieveScript(service, scriptDbid)

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgTenant = tenant.createCfgObject(service)

            with(cfgTenant) {
                assertThat(name, equalTo(tenant.name))
                assertThat(defaultCapacityRuleDBID, equalTo(scriptDbid))
                assertThat(defaultContractDBID, equalTo(objectiveTableDbid))
                assertThat(chargeableNumber, equalTo(tenant.chargeableNumber))
                assertThat(parentTenantDBID, equalTo(DEFAULT_TENANT_DBID))
                assertThat(password, equalTo(tenant.password))
                assertThat(state, equalTo(ConfigurationObjects.toCfgObjectState(tenant.state)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(tenant.userProperties))
            }
        }
    }
}

private fun mockCfgTenant(): CfgTenant {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    val capacityRule = mockCfgScript(tenant.defaultCapacityRule!!.primaryKey)
    val contract = mockCfgObjectiveTable(tenant.defaultContract!!.primaryKey)
    val parentTenant = mockCfgTenant(tenant.parentTenant!!.primaryKey)

    return mockCfgTenant(tenant.name).also {
        every { it.configurationService } returns service
        every { it.password } returns tenant.password
        every { it.state } returns CFGEnabled
        every { it.userProperties } returns mockKeyValueCollection()
        every { it.chargeableNumber } returns tenant.chargeableNumber
        every { it.defaultCapacityRule } returns capacityRule
        every { it.defaultContract } returns contract
        every { it.parentTenant } returns parentTenant
        every { it.folderId } returns DEFAULT_FOLDER_DBID
    }
}
