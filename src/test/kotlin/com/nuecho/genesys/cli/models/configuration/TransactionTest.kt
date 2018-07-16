package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgTransactionType.CFGTRTList
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTransaction
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgTransactionType
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
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

private const val NAME = "foo"
private val CFGTRTLIST_TYPE = CFGTRTList
private val transaction = Transaction(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    type = CFGTRTLIST_TYPE.toShortName(),
    alias = "bar",
    recordPeriod = 0,
    description = "some description",
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class TransactionTest : ConfigurationObjectTest(
    transaction,
    Transaction(tenant = DEFAULT_TENANT_REFERENCE, name = NAME, type = CFGTRTLIST_TYPE.toShortName()),
    setOf(ALIAS),
    Transaction(mockCfgTransaction())
) {
    @Test
    fun `createCfgObject should properly create CfgTransaction`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgTransaction::class.java, any()) } returns null
        mockRetrieveTenant(service)

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgTransaction = transaction.createCfgObject(service)

            with(cfgTransaction) {
                assertThat(name, equalTo(transaction.name))
                assertThat(alias, equalTo(transaction.alias))
                assertThat(type, equalTo(toCfgTransactionType(transaction.type)))
                assertThat(recordPeriod, equalTo(transaction.recordPeriod))
                assertThat(description, equalTo(transaction.description))
                assertThat(state, equalTo(toCfgObjectState(transaction.state)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(transaction.userProperties))
            }
        }
    }
}

private fun mockCfgTransaction() = mockCfgTransaction(transaction.name).apply {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    every { configurationService } returns service
    every { alias } returns transaction.alias
    every { description } returns transaction.description
    every { recordPeriod } returns transaction.recordPeriod
    every { type } returns CFGTRTLIST_TYPE
    every { state } returns CfgObjectState.CFGEnabled
    every { userProperties } returns mockKeyValueCollection()
    every { folderId } returns DEFAULT_OBJECT_DBID
}
