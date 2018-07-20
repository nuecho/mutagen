package com.nuecho.genesys.cli.models.configuration

import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.getResourceAsString
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
