package com.nuecho.genesys.cli

import com.fasterxml.jackson.databind.JsonNode
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.preferences.environment.Environments
import java.io.BufferedReader
import java.io.File

object TestResources {
    fun loadEnvironments(path: String): Environments {
        val environments = toPreferenceFile(path).readText()
        return Environments.load(environments)
    }

    fun toPreferenceFile(path: String): File =
        File(ClassLoader.getSystemClassLoader().getResource(".mutagen/$path").toURI())

    fun loadRawConfiguration(path: String): JsonNode =
        defaultJsonObjectMapper().readTree(getTestResource(path))

    fun loadJsonConfiguration(path: String): Configuration =
        defaultJsonObjectMapper().readValue(getTestResource(path), Configuration::class.java)

    fun <T> loadJsonConfiguration(path: String, objectType: Class<T>): T =
        defaultJsonObjectMapper().readValue(getTestResource(path), objectType)

    fun getTestResource(path: String) =
        ClassLoader.getSystemClassLoader().getResource("com/nuecho/genesys/cli/$path")
}

fun Class<*>.getResourceAsString(name: String): String =
    getResourceAsStream(name).bufferedReader().use(BufferedReader::readText)
