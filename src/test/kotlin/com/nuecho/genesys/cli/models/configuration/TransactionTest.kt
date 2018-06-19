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
import io.kotlintest.matchers.shouldBe
import io.mockk.every

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
    init {
        "Transaction.updateCfgObject should properly create CfgTransaction" {
            val service = mockConfService()
            every { service.retrieveObject(CfgTransaction::class.java, any()) } returns null
            mockRetrieveTenant(service)

            val cfgTransaction = transaction.updateCfgObject(service)

            with(cfgTransaction) {
                name shouldBe transaction.name
                alias shouldBe transaction.alias
                type shouldBe toCfgTransactionType(transaction.type)
                recordPeriod shouldBe transaction.recordPeriod
                description shouldBe transaction.description
                state shouldBe toCfgObjectState(transaction.state)
                userProperties.asCategorizedProperties() shouldBe transaction.userProperties
            }
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
