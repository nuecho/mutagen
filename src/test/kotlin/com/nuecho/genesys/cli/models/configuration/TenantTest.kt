package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.mockk

private val tenant = Tenant(
    defaultCapacityRule = "capacityRule",
    defaultContract = "contract",
    chargeableNumber = "123",
    name = "foo",
    password = "password",
    parentTenant = "parent",
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties()
)

class TenantTest : ConfigurationObjectTest(tenant, Tenant("foo")) {
    init {
        "CfgTenant initialized Tenant should properly serialize" {
            val tenant = Tenant(mockCfgTenant())
            checkSerialization(tenant, "tenant")
        }
    }

    private fun mockCfgTenant(): CfgTenant {

        val capacityRule = mockk<CfgScript>()
        every { capacityRule.name } returns tenant.defaultCapacityRule

        val contract = mockk<CfgObjectiveTable>()
        every { contract.name } returns tenant.defaultContract

        val parentTenant = mockk<CfgTenant>()
        every { parentTenant.name } returns tenant.parentTenant

        val cfgTenant = mockk<CfgTenant>()

        every { cfgTenant.password } returns tenant.password
        every { cfgTenant.name } returns tenant.name
        every { cfgTenant.state } returns CFGEnabled
        every { cfgTenant.userProperties } returns mockKeyValueCollection()
        every { cfgTenant.chargeableNumber } returns tenant.chargeableNumber
        every { cfgTenant.isServiceProvider } returns CfgFlag.CFGFalse
        every { cfgTenant.defaultCapacityRule } returns capacityRule
        every { cfgTenant.defaultContract } returns contract
        every { cfgTenant.parentTenant } returns parentTenant

        return cfgTenant
    }
}
