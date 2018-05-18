package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgTransactionType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTransaction
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every

private val transaction = Transaction(
    name = "foo",
    alias = "bar",
    recordPeriod = 0,
    description = "some description",
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = defaultProperties()
)

class TransactionTest : ConfigurationObjectTest(transaction, Transaction("foo"), Transaction(mockCfgTransaction())) {

    init {
        val service = mockConfService()

        "Transaction.updateCfgObject should properly create CfgTransaction" {
            every { service.retrieveObject(CfgTransaction::class.java, any()) } returns null

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

private fun mockCfgTransaction() = mockCfgTransaction(transaction.name).also {
    every { it.alias } returns transaction.alias
    every { it.description } returns transaction.description
    every { it.recordPeriod } returns transaction.recordPeriod
    every { it.type } returns CfgTransactionType.CFGTRTNoTransactionType
    every { it.state } returns CfgObjectState.CFGEnabled
    every { it.userProperties } returns mockKeyValueCollection()
}
