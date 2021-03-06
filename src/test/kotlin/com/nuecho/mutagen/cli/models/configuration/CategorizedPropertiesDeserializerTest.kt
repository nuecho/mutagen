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

package com.nuecho.mutagen.cli.models.configuration

import com.fasterxml.jackson.databind.module.SimpleModule
import com.nuecho.mutagen.cli.core.defaultJsonObjectMapper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
class CategorizedPropertiesDeserializerTest {

    val mapper = defaultJsonObjectMapper().copy()
    val module = SimpleModule().apply { addDeserializer(Map::class.java, CategorizedPropertiesDeserializer()) }

    @BeforeAll
    fun init() {
        mapper.registerModule(module)
    }

    @Test
    fun `CategorizedProperties should deserialize properly`() {
        val expected = mapOf(
            "section" to mapOf(
                "number" to 456,
                "string" to "def",
                "bytes" to "def".toByteArray()
            )
        )

        val jsonObject = """
                {
                    "section": {
                        "number": 456,
                        "string": "def",
                        "bytes": [
                            "ZGVm"
                        ]
                    }
                }
            """

        @Suppress("UNCHECKED_CAST")
        val actual = mapper.readValue(jsonObject, Map::class.java) as CategorizedProperties

        assertThat(actual["section"]?.get("number") as Number, equalTo(expected["section"]?.get("number")))
        assertThat(actual["section"]?.get("string") as String, equalTo(expected["section"]?.get("string")))
        checkUserProperties(actual, expected)
    }

    @Test
    fun `CategorizedProperties with no section should throw`() {
        val jsonObject = """
                {
                    "toto": "tata"
                }
            """

        assertThrows(InvalidKeyValueCollectionException::class.java) {
            mapper.readValue(jsonObject, Map::class.java)
        }
    }
}

private fun checkUserProperties(
    expectedUserProperties: CategorizedProperties,
    actualUserProperties: CategorizedProperties
) {
    // Ensure that byte arrays are properly deserialized (GC-60)
    val actualByteArray = actualUserProperties["section"]?.get("bytes") as ByteArray
    val expectedByteArray = expectedUserProperties["section"]?.get("bytes") as ByteArray
    assertThat(actualByteArray, equalTo(expectedByteArray))
}
