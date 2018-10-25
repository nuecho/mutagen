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

import com.nuecho.mutagen.cli.core.defaultJsonObjectMapper
import com.nuecho.mutagen.cli.getResourceAsString
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ConfigurationTest {

    @Test
    fun `should convert to list`() {
        val configuration = defaultJsonObjectMapper().readValue(
            javaClass.getResourceAsStream("configuration.json"),
            Configuration::class.java
        )

        assertThat(configuration.asList, hasSize(3))
    }

    @Test
    fun `should convert to map by reference`() {
        val configuration = defaultJsonObjectMapper().readValue(
            javaClass.getResourceAsStream("configuration.json"),
            Configuration::class.java
        )

        assertThat(configuration.asMapByReference.entries, hasSize(3))
    }

    @Test
    fun `should interpolate environment variables in configuration file string`() {

        val expected = javaClass.getResourceAsString("configuration_interpolated.json")

        val input = javaClass.getResourceAsString("configuration_before_interpolation.json")
        val actual = Configuration.interpolateVariables(input, mapOf("FOO" to "foo", "BAR_1234" to "bar"))

        assertThat(actual, equalTo(expected))
    }

    @Test
    fun `should throw on environment variables interpolation if undefined variable`() {
        val input = javaClass.getResourceAsString("configuration_before_interpolation.json")

        assertThrows(UndefinedVariableException::class.java, {
            Configuration.interpolateVariables(input, mapOf("FOO" to "foo"))
        })
    }
}
