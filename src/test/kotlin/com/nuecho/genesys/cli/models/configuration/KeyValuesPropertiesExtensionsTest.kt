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

package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.commons.collections.KeyValueCollection
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class KeyValuesPropertiesExtensionsTest {
    @Test
    fun `asCategorizedProperties() should return the right map when passed a valid KeyValueCollection`() {
        val actual = KeyValueCollection().apply {
            this.addObject("section1", KeyValueCollection().apply { addString("string", "string") })
            this.addObject("section2", KeyValueCollection().apply { addInt("int", 1) })
        }

        assertThat(
            actual.asCategorizedProperties(),
            equalTo(
                mapOf(
                    "section1" to mapOf("string" to "string"),
                    "section2" to mapOf("int" to 1)
                )
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
