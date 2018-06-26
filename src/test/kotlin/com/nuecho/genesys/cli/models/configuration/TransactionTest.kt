package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgTransactionType.CFGTRTList
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTransaction
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgTransactionType
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val NAME = "foo"
private val TYPE = CFGTRTList
private val transaction = Transaction(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    type = TYPE.toShortName(),
    alias = "bar",
    recordPeriod = 0,
    description = "some description",
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = defaultProperties()
)

class TransactionTest : ConfigurationObjectTest(
    transaction,
    Transaction(tenant = DEFAULT_TENANT_REFERENCE, name = NAME, type = TYPE.toShortName()),
    Transaction(mockCfgTransaction())
) {
    @Test
    fun `updateCfgObject should properly create CfgTransaction`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgTransaction::class.java, any()) } returns null
        mockRetrieveTenant(service)

        val cfgTransaction = transaction.updateCfgObject(service)

        with(cfgTransaction) {
            assertEquals(transaction.name, name)
            assertEquals(transaction.alias, alias)
            assertEquals(toCfgTransactionType(transaction.type), type)
            assertEquals(transaction.recordPeriod, recordPeriod)
            assertEquals(transaction.description, description)
            assertEquals(toCfgObjectState(transaction.state), state)
            assertEquals(transaction.userProperties, userProperties.asCategorizedProperties())
        }
    }
}

private fun mockCfgTransaction() = mockCfgTransaction(transaction.name).apply {
    every { alias } returns transaction.alias
    every { description } returns transaction.description
    every { recordPeriod } returns transaction.recordPeriod
    every { type } returns TYPE
    every { state } returns CfgObjectState.CFGEnabled
    every { userProperties } returns mockKeyValueCollection()
}
