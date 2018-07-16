package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPCustomer
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgGVPCustomer
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.GVPResellerReference
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
    reseller = GVPResellerReference("areseller", DEFAULT_TENANT_REFERENCE),
    channel = "achannel",
    notes = "some notes",
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class GVPCustomerTest : ConfigurationObjectTest(
    gvpCustomer,
    GVPCustomer(name = NAME),
    setOf(CHANNEL, RESELLER, TENANT),
    GVPCustomer(mockCfgGVPCustomer())
) {
    @Test
    fun `createCfgObject should properly create CfgGVPCustomer`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgGVPCustomer::class.java, any()) } returns null
        mockRetrieveTenant(service)
        mockRetrieveReseller(service)
        mockRetrieveTimeZone(service)

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgGVPCustomer = gvpCustomer.createCfgObject(service)

            with(cfgGVPCustomer) {
                assertThat(name, equalTo(gvpCustomer.name))
                assertThat(state, equalTo(ConfigurationObjects.toCfgObjectState(gvpCustomer.state)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(gvpCustomer.userProperties))
            }
        }
    }
}

private fun mockCfgGVPCustomer() = mockCfgGVPCustomer(gvpCustomer.name).apply {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    val resellerMock = ConfigurationObjectMocks.mockCfgGVPReseller(gvpCustomer.reseller!!.primaryKey)
    every { configurationService } returns service
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
    every { folderId } returns DEFAULT_OBJECT_DBID
}
