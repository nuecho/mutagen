package com.nuecho.genesys.cli

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nuecho.genesys.cli.preferences.environment.Environments
import java.io.File

object TestResources {
    fun loadEnvironments(path: String): Environments {
        val environments = toFile(path).readText()

        return Environments.load(environments)
    }

    fun toFile(path: String): File = File(ClassLoader.getSystemClassLoader().getResource("preferences/$path").toURI())

    fun loadJsonConfiguration(path: String): JsonNode {
        val configuration = ClassLoader.getSystemClassLoader().getResource("configuration/$path")
        return jacksonObjectMapper().readTree(configuration)
    }
}
