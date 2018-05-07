package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.commons.collections.KeyValueCollection
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.StringSpec

class KeyValuesPropertiesExtensionsTest : StringSpec() {
    init {
        "KeyValueCollection.asCategorizedProperties() should return the right map when passed a valid KeyValueCollection" {
            val actual = KeyValueCollection().apply {
                this.addObject("section1", KeyValueCollection().apply { addString("string", "string") })
                this.addObject("section2", KeyValueCollection().apply { addInt("int", 1) })
            }

            actual.asCategorizedProperties() shouldEqual mapOf(
                "section1" to mapOf("string" to "string"),
                "section2" to mapOf("int" to 1)
            )
        }

        "KeyValueCollection.asCategorizedProperties() should throw when passed an invalid KeyValueCollection" {
            val actual = KeyValueCollection().apply {
                this.addString("string", "string")
            }

            shouldThrow<InvalidKeyValueCollectionException> {
                actual.asCategorizedProperties()
            }
        }
    }
}
