/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nuecho.mutagen.cli.models.configuration.reference

import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.configuration.protocol.types.CfgTransactionType.CFGTRTMacro
import com.genesyslab.platform.configuration.protocol.types.CfgTransactionType.CFGTRTStatType
import com.nuecho.mutagen.cli.TestResources.loadJsonConfiguration
import com.nuecho.mutagen.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_NAME
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.mutagen.cli.services.ServiceMocks.mockConfService
import com.nuecho.mutagen.cli.toShortName
import io.mockk.every
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test

private const val NAME = "transaction"
private val TYPE = CFGTRTMacro

class TransactionReferenceTest {
    private val transactionReference = TransactionReference(
        name = NAME,
        type = TYPE,
        tenant = DEFAULT_TENANT_REFERENCE
    )

    @Test
    fun `TransactionReference should be serialized as a JSON Object without tenant references`() {
        checkSerialization(transactionReference, "reference/transaction_reference")
    }

    @Test
    fun `TransactionReference should properly deserialize`() {
        val deserializedTransactionReference = loadJsonConfiguration(
            "models/configuration/reference/transaction_reference.json",
            TransactionReference::class.java
        )

        assertThat(deserializedTransactionReference.tenant, `is`(nullValue()))
        assertThat(deserializedTransactionReference.type, equalTo(TYPE.toShortName()))
        assertThat(deserializedTransactionReference.name, equalTo(NAME))
    }

    @Test
    fun `TransactionReference toQuery should create the proper query`() {
        val cfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME)

        val service = mockConfService()
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant

        val query = transactionReference.toQuery(service)
        assertThat(query.tenantDbid, equalTo(DEFAULT_TENANT_DBID))
        assertThat(query.objectType, equalTo(TYPE))
        assertThat(query.name, equalTo(NAME))
    }

    @Test
    fun `toString should generate the proper string`() {
        assertThat("name: '$NAME', type: '${TYPE.toShortName()}', tenant: '$DEFAULT_TENANT_NAME'", equalTo(transactionReference.toString()))
    }

    @Test
    fun `TransactionReference should be sorted properly`() {
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
        assertThat(sortedList, contains(transactionReference1, transactionReference2, transactionReference3, transactionReference4, transactionReference5))
    }
}
