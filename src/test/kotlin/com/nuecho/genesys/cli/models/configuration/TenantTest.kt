package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.nuecho.genesys.cli.TestResources
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk

class TenantTest : StringSpec() {
    private val tenant = Tenant(
        defaultCapacityRule = "capacityRule",
        defaultContract = "contract",
        chargeableNumber = "123",
        name = "foo",
        password = "password",
        parentTenant = "parent",
        state = CfgObjectState.CFGEnabled.toShortName(),
        userProperties = defaultProperties()
    )

    init {
        "empty Tenant should properly serialize" {
            checkSerialization(Tenant(name = "foo"), "empty_tenant")
        }

        "fully initialized Tenant should properly serialize" {
            checkSerialization(tenant, "tenant")
        }

        "Tenant should properly deserialize" {
            val tenant = TestResources.loadJsonConfiguration(
                "models/configuration/tenant.json",
                Tenant::class.java
            )

            checkSerialization(tenant, "tenant")

            val actualByteArray = tenant.userProperties!!["bytes"] as ByteArray
            val expectedByteArray = tenant.userProperties!!["bytes"] as ByteArray
            actualByteArray.contentEquals(expectedByteArray) shouldBe true
        }

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
        every { cfgTenant.state } returns CfgObjectState.CFGEnabled
        every { cfgTenant.userProperties } returns mockKeyValueCollection()
        every { cfgTenant.chargeableNumber } returns tenant.chargeableNumber
        every { cfgTenant.isServiceProvider } returns CfgFlag.CFGFalse
        every { cfgTenant.defaultCapacityRule } returns capacityRule
        every { cfgTenant.defaultContract } returns contract
        every { cfgTenant.parentTenant } returns parentTenant

        return cfgTenant
    }
}
