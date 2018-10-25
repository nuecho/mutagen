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

import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.nuecho.mutagen.cli.TestResources.loadJsonConfiguration
import com.nuecho.mutagen.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_NAME
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.mutagen.cli.services.ServiceMocks
import com.nuecho.mutagen.cli.services.getObjectDbid
import io.mockk.every
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val DBID = 102
private const val ENUMERATOR = "enumerator"
private val ENUMERATOR_REFERENCE = EnumeratorReference(ENUMERATOR, DEFAULT_TENANT_REFERENCE)
private const val NAME = "enumeratorValue"

class EnumeratorValueReferenceTest {
    private val enumeratorValueReference = EnumeratorValueReference(
        name = NAME,
        enumerator = ENUMERATOR_REFERENCE
    )

    @Test
    fun `EnumeratorValueReference should be serialized as a JSON Object without tenant references`() {
        checkSerialization(enumeratorValueReference, "reference/enumerator_value_reference")
    }

    @Test
    fun `EnumeratorValueReference should properly deserialize`() {
        val deserializedEnumeratorValueReference = loadJsonConfiguration(
            "models/configuration/reference/enumerator_value_reference.json",
            EnumeratorValueReference::class.java
        )
        val expectedEnumeratorValueReference = EnumeratorValueReference(NAME, EnumeratorReference(ENUMERATOR, null))

        assertThat(deserializedEnumeratorValueReference, equalTo(expectedEnumeratorValueReference))
    }

    @Test
    fun `EnumeratorValueReference toQuery should create the proper query`() {
        val cfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME)

        val service = ServiceMocks.mockConfService()
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant
        every { service.getObjectDbid(ENUMERATOR_REFERENCE) } returns DBID
        every { service.retrieveObject(ENUMERATOR_REFERENCE.cfgObjectClass, any()) } answers { CfgEnumerator(service).apply { dbid = DBID } }

        val query = enumeratorValueReference.toQuery(service)
        assertThat(query.enumeratorDbid, equalTo(DBID))
        assertThat(query.name, equalTo(NAME))
    }

    @Test
    fun `EnumeratorValueReference toString() should generate the proper String`() {
        assertThat("name: '$NAME', enumerator: '$ENUMERATOR_REFERENCE'", equalTo(enumeratorValueReference.toString()))
    }

    @Test
    fun `EnumeratorValueReference should be sorted properly`() {
        val enumeratorValueReference1 = EnumeratorValueReference(
            enumerator = EnumeratorReference(NAME, TenantReference("aaaTenant")),
            name = "aaa"
        )

        val enumeratorValueReference2 = EnumeratorValueReference(
            enumerator = EnumeratorReference(NAME, DEFAULT_TENANT_REFERENCE),
            name = "aaa"
        )
        val enumeratorValueReference3 = EnumeratorValueReference(
            enumerator = EnumeratorReference("${NAME}aaa", DEFAULT_TENANT_REFERENCE),
            name = "aaa"
        )

        val enumeratorValueReference4 = EnumeratorValueReference(
            enumerator = EnumeratorReference("${NAME}aaa", DEFAULT_TENANT_REFERENCE),
            name = "ccc"
        )

        val enumeratorValueReference5 = EnumeratorValueReference(
            enumerator = EnumeratorReference("${NAME}bbb", DEFAULT_TENANT_REFERENCE),
            name = "ccc"
        )

        val sortedList = listOf(
            enumeratorValueReference5,
            enumeratorValueReference4,
            enumeratorValueReference3,
            enumeratorValueReference2,
            enumeratorValueReference1
        ).sorted()
        assertThat(
            sortedList, contains(
                enumeratorValueReference1,
                enumeratorValueReference2,
                enumeratorValueReference3,
                enumeratorValueReference4,
                enumeratorValueReference5
            )
        )
    }
}
