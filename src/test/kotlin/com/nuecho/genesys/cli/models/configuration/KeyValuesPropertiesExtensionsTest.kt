package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.commons.collections.KeyValueCollection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class KeyValuesPropertiesExtensionsTest {
    @Test
    fun `asCategorizedProperties() should return the right map when passed a valid KeyValueCollection`() {
        val actual = KeyValueCollection().apply {
            this.addObject("section1", KeyValueCollection().apply { addString("string", "string") })
            this.addObject("section2", KeyValueCollection().apply { addInt("int", 1) })
        }

        assertEquals(
            actual.asCategorizedProperties(),
            mapOf(
                "section1" to mapOf("string" to "string"),
                "section2" to mapOf("int" to 1)
            )
        )
    }

    @Test
    fun `asCategorizedProperties() should throw when passed an invalid KeyValueCollection`() {
        val actual = KeyValueCollection().apply {
            this.addString("string", "string")
        }

        assertThrows(InvalidKeyValueCollectionException::class.java) {
            actual.asCategorizedProperties()
        }
    }
}
