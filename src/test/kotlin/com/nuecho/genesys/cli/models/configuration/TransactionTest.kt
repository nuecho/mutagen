package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgTransactionType
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.retrieveTransaction
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.staticMockk
import io.mockk.use

private val transaction = Transaction(
    name = "foo",
    alias = "bar",
    recordPeriod = 0,
    description = "some description",
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = defaultProperties()
)

class TransactionTest : ConfigurationObjectTest(transaction, Transaction("foo")) {

    init {
        "CfgTransaction initialized Transaction should properly serialize" {
            val Transaction = Transaction(mockCfgTransaction())
            checkSerialization(Transaction, "transaction")
        }
        "Transaction.updateCfgObject should properly create CfgTransaction" {
            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {

                every { service.retrieveTransaction(any()) } returns null

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
                    userProperties.size shouldBe 4
                }
            }
        }
    }

    private fun mockCfgTransaction(): CfgTransaction {

        val cfgTransaction = mockk<CfgTransaction>()
        every { cfgTransaction.alias } returns transaction.alias
        every { cfgTransaction.description } returns transaction.description
        every { cfgTransaction.recordPeriod } returns transaction.recordPeriod
        every { cfgTransaction.type } returns CfgTransactionType.CFGTRTNoTransactionType
        every { cfgTransaction.name } returns transaction.name
        every { cfgTransaction.state } returns CfgObjectState.CFGEnabled
        every { cfgTransaction.userProperties } returns mockKeyValueCollection()

        return cfgTransaction
    }
}
