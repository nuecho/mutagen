package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPCustomer
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgGVPCustomer
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.GVPResellerReference
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveReseller
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTimeZone
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every

private const val NAME = "name"
private val gvpCustomer = GVPCustomer(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    reseller = GVPResellerReference("areseller", DEFAULT_TENANT_REFERENCE),
    channel = "achannel",
    notes = "some notes",
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = defaultProperties()
)

class GVPCustomerTest : ConfigurationObjectTest(
    gvpCustomer,
    GVPCustomer(name = NAME),
    GVPCustomer(mockCfgGVPCustomer())
) {
    init {
        "GVPCustomer.updateCfgObject should properly create CfgGVPCustomer" {
            val service = mockConfService()
            every { service.retrieveObject(CfgGVPCustomer::class.java, any()) } returns null
            mockRetrieveTenant(service)
            mockRetrieveReseller(service)
            mockRetrieveTimeZone(service)

            val (status, cfgObject) = gvpCustomer.updateCfgObject(service)
            val cfgGVPCustomer = cfgObject as CfgGVPCustomer

            status shouldBe CREATED

            with(cfgGVPCustomer) {
                name shouldBe gvpCustomer.name
                state shouldBe ConfigurationObjects.toCfgObjectState(gvpCustomer.state)
                userProperties.asCategorizedProperties() shouldBe gvpCustomer.userProperties
            }
        }
    }
}

private fun mockCfgGVPCustomer() = mockCfgGVPCustomer(gvpCustomer.name).apply {
    val resellerMock = ConfigurationObjectMocks.mockCfgGVPReseller(gvpCustomer.reseller!!.primaryKey)

    every { state } returns toCfgObjectState(gvpCustomer.state)
    every { channel } returns gvpCustomer.channel
    every { reseller } returns resellerMock

    every { notes } returns gvpCustomer.notes
    every { displayName } returns null
    every { isAdminCustomer } returns toCfgFlag(gvpCustomer.isAdminCustomer)
    every { isProvisioned } returns toCfgFlag(gvpCustomer.isProvisioned)
    every { notes } returns gvpCustomer.notes
    every { timeZone } returns null

    every { userProperties } returns mockKeyValueCollection()
}
