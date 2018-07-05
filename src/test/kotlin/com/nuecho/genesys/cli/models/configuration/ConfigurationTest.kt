package com.nuecho.genesys.cli.models.configuration

import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
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
}
