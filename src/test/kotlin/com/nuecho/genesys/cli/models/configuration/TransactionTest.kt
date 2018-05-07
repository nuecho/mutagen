package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgTransactionType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTransaction
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every

private const val NAME = "foo"
private val transaction = Transaction(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    alias = "bar",
    recordPeriod = 0,
    description = "some description",
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = defaultProperties()
)

class TransactionTest : ConfigurationObjectTest(
    transaction,
    Transaction(tenant = DEFAULT_TENANT_REFERENCE, name = NAME),
    Transaction(mockCfgTransaction())
) {
    init {
        "Transaction.updateCfgObject should properly create CfgTransaction" {
            val service = mockConfService()
            every { service.retrieveObject(CfgTransaction::class.java, any()) } returns null
            mockRetrieveTenant(service)

            val (status, cfgObject) = transaction.updateCfgObject(service)
            val cfgTransaction = cfgObject as CfgTransaction

            status shouldBe ConfigurationObjectUpdateStatus.CREATED

            with(cfgTransaction) {
                name shouldBe transaction.name
                alias shouldBe transaction.alias
                type shouldBe ConfigurationObjects.toCfgTransactionType(transaction.type)
                recordPeriod shouldBe transaction.recordPeriod
                description shouldBe transaction.description
                state shouldBe ConfigurationObjects.toCfgObjectState(transaction.state)
                userProperties.asCategorizedProperties() shouldBe transaction.userProperties
            }
        }
    }
}

private fun mockCfgTransaction() = mockCfgTransaction(transaction.name).apply {
    every { alias } returns transaction.alias
    every { description } returns transaction.description
    every { recordPeriod } returns transaction.recordPeriod
    every { type } returns CfgTransactionType.CFGTRTNoTransactionType
    every { state } returns CfgObjectState.CFGEnabled
    every { userProperties } returns mockKeyValueCollection()
}
