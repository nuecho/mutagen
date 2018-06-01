package com.nuecho.genesys.cli.models.configuration.reference

import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.configuration.protocol.types.CfgTransactionType.CFGTRTMacro
import com.genesyslab.platform.configuration.protocol.types.CfgTransactionType.CFGTRTStatType
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every

private const val NAME = "transaction"
private val TYPE = CFGTRTMacro

class TransactionReferenceTest : StringSpec() {
    private val transactionReference = TransactionReference(
        name = NAME,
        type = TYPE,
        tenant = DEFAULT_TENANT_REFERENCE
    )

    init {
        "TransactionReference should be serialized as a JSON Object without tenant references" {
            checkSerialization(transactionReference, "reference/transaction_reference")
        }

        "TransactionReference should properly deserialize" {
            val deserializedTransactionReference = loadJsonConfiguration(
                "models/configuration/reference/transaction_reference.json",
                TransactionReference::class.java
            )

            deserializedTransactionReference.tenant shouldBe null
            deserializedTransactionReference.type shouldBe TYPE.toShortName()
            deserializedTransactionReference.name shouldBe NAME
        }

        "TransactionReference should create the proper query" {
            val cfgTenant = mockCfgTenant(ConfigurationObjectMocks.DEFAULT_TENANT)

            val service = mockConfService()
            every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant

            val query = transactionReference.toQuery(service)
            query.tenantDbid shouldBe DEFAULT_TENANT_DBID
            query.objectType shouldBe TYPE
            query.name shouldBe NAME
        }

        "TransactionReference.toString" {
            transactionReference.toString() shouldBe "name: '$NAME', type: '${TYPE.toShortName()}', tenant: '${ConfigurationObjectMocks.DEFAULT_TENANT}'"
        }

        "TransactionReference should be sorted properly" {
            val transactionReference1 = TransactionReference(
                name = "aaa",
                type = CFGTRTMacro,
                tenant = TenantReference("aaaTenant")
            )

            val transactionReference2 = TransactionReference(
                name = "aaa",
                type = CFGTRTMacro,
                tenant = DEFAULT_TENANT_REFERENCE
            )

            val transactionReference3 = TransactionReference(
                name = "aaa",
                type = CFGTRTStatType,
                tenant = DEFAULT_TENANT_REFERENCE
            )

            val transactionReference4 = TransactionReference(
                name = "bbb",
                type = CFGTRTStatType,
                tenant = DEFAULT_TENANT_REFERENCE
            )

            val transactionReference5 = TransactionReference(
                name = "ccc",
                type = CFGTRTStatType,
                tenant = DEFAULT_TENANT_REFERENCE
            )

            val sortedList = listOf(transactionReference5, transactionReference4, transactionReference3, transactionReference2, transactionReference1).sorted()
            sortedList shouldBe listOf(transactionReference1, transactionReference2, transactionReference3, transactionReference4, transactionReference5)
        }
    }
}
