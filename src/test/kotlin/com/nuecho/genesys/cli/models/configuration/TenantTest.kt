package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT
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
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveObjectiveTable
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveScript
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private val tenant = Tenant(
    name = "foo",
    defaultCapacityRule = ScriptReference("capacityRule", DEFAULT_TENANT_REFERENCE),
    defaultContract = ObjectiveTableReference("contract", DEFAULT_TENANT_REFERENCE),
    chargeableNumber = "123",
    password = "password",
    parentTenant = TenantReference("parent"),
    state = CFGEnabled.toShortName(),
    serviceProvider = false,
    userProperties = defaultProperties()
)

class TenantTest : ConfigurationObjectTest(tenant, Tenant("foo"), Tenant(mockCfgTenant())) {
    @Test
    fun `updateCfgObject should properly create CfgTenant`() {
        val defaultTenant = mockCfgTenant(DEFAULT_TENANT)
        val service = mockConfService()
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns null andThen defaultTenant
        mockRetrieveObjectiveTable(service)
        mockRetrieveScript(service)

        val cfgTenant = tenant.updateCfgObject(service)

        with(cfgTenant) {
            assertEquals(tenant.name, name)
            assertEquals(DEFAULT_OBJECT_DBID, defaultCapacityRuleDBID)
            assertEquals(defaultContractDBID, DEFAULT_OBJECT_DBID)
            assertEquals(chargeableNumber, tenant.chargeableNumber)
            assertEquals(parentTenantDBID, DEFAULT_TENANT_DBID)
            assertEquals(password, tenant.password)
            assertEquals(state, ConfigurationObjects.toCfgObjectState(tenant.state))
            assertEquals(userProperties.asCategorizedProperties(), tenant.userProperties)
        }
    }
}

private fun mockCfgTenant(): CfgTenant {
    val capacityRule = mockCfgScript(tenant.defaultCapacityRule!!.primaryKey)
    val contract = mockCfgObjectiveTable(tenant.defaultContract!!.primaryKey)
    val parentTenant = mockCfgTenant(tenant.parentTenant!!.primaryKey)

    return mockCfgTenant(tenant.name).also {
        every { it.password } returns tenant.password
        every { it.state } returns CFGEnabled
        every { it.userProperties } returns mockKeyValueCollection()
        every { it.chargeableNumber } returns tenant.chargeableNumber
        every { it.isServiceProvider } returns CfgFlag.CFGFalse
        every { it.defaultCapacityRule } returns capacityRule
        every { it.defaultContract } returns contract
        every { it.parentTenant } returns parentTenant
    }
}
