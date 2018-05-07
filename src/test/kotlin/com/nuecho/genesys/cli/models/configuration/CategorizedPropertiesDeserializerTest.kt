package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.databind.module.SimpleModule
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.StringSpec

class CategorizedPropertiesDeserializerTest : StringSpec() {

    init {
        val mapper = defaultJsonObjectMapper().copy()
        val module = SimpleModule().apply { addDeserializer(Map::class.java, CategorizedPropertiesDeserializer()) }
        mapper.registerModule(module)

        "CategorizedProperties should deserialize properly" {
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

            actual["section"]?.get("number") shouldBe expected["section"]?.get("number")
            actual["section"]?.get("string") shouldBe expected["section"]?.get("string")
            checkUserProperties(actual, expected)
        }

        "CategorizedProperties with no section should throw" {
            val jsonObject = """
                {
                    "toto": "tata"
                }
            """

            shouldThrow<InvalidKeyValueCollectionException> {
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
        actualByteArray.contentEquals(expectedByteArray) shouldBe true
    }
}
