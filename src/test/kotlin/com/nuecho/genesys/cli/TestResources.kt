package com.nuecho.genesys.cli

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nuecho.genesys.cli.preferences.environment.Environments
import java.io.File

object TestResources {
    fun loadEnvironments(path: String): Environments {
        val environments = toPreferenceFile(path).readText()

        return Environments.load(environments)
    }

    fun toPreferenceFile(path: String): File =
        File(ClassLoader.getSystemClassLoader().getResource(".mutagen/$path").toURI())

    fun loadJsonConfiguration(path: String): JsonNode {
        val configuration = ClassLoader.getSystemClassLoader().getResource("configuration/$path")
        return jacksonObjectMapper().readTree(configuration)
    }
}
