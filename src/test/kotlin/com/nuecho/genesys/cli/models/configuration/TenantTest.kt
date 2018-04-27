package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgObjectiveTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks
import com.nuecho.genesys.cli.services.retrieveTenant
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every
import io.mockk.staticMockk
import io.mockk.use

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
        "Tenant.updateCfgObject should properly create CfgTenant" {
            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {

                val dbid = 101

                every { service.retrieveTenant(any()) } returns null
                ConfServiceExtensionMocks.mockRetrieveObjectiveTable(service, dbid)
                ConfServiceExtensionMocks.mockRetrieveScript(service, dbid)

                val (status, cfgObject) = tenant.updateCfgObject(service)
                val cfgTenant = cfgObject as CfgTenant

                status shouldBe ConfigurationObjectUpdateStatus.CREATED

                with(cfgTenant) {
                    name shouldBe tenant.name
                    defaultCapacityRuleDBID shouldBe dbid
                    defaultContractDBID shouldBe dbid
                    chargeableNumber shouldBe tenant.chargeableNumber
                    parentTenant shouldBe null
                    password shouldBe tenant.password
                    state shouldBe ConfigurationObjects.toCfgObjectState(tenant.state)
                    userProperties.size shouldBe 4
                }
            }
        }
    }

    private fun mockCfgTenant(): CfgTenant {
        val capacityRule = mockCfgScript(tenant.defaultCapacityRule)
        val contract = mockCfgObjectiveTable(tenant.defaultContract)
        val parentTenant = mockCfgTenant(tenant.parentTenant)

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
}
