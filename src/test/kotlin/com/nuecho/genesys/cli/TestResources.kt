package com.nuecho.genesys.cli

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nuecho.genesys.cli.preferences.environment.Environments

object TestResources {
    fun loadEnvironments(path: String): Environments {
        val environments = ClassLoader
            .getSystemClassLoader()
            .getResource("preferences/$path")
            .readText()

        return Environments.load(environments)
    }

    fun loadJsonConfiguration(path: String): JsonNode {
        val configuration = ClassLoader.getSystemClassLoader().getResource("configuration/$path")
        return jacksonObjectMapper().readTree(configuration)
    }
}
