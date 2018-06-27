package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.databind.module.SimpleModule
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
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
