package com.nuecho.genesys.cli

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.preferences.environment.Environments
import java.io.File

object TestResources {
    fun loadEnvironments(path: String): Environments {
        val environments = toPreferenceFile(path).readText()

        return Environments.load(environments)
    }

    fun toPreferenceFile(path: String): File =
        File(ClassLoader.getSystemClassLoader().getResource(".mutagen/$path").toURI())

    fun loadRawConfiguration(path: String) = jacksonObjectMapper().readTree(loadConfigurationFile(path))

    fun loadJsonConfiguration(path: String) =
        jacksonObjectMapper().readValue(loadConfigurationFile(path), Configuration::class.java)

    private fun loadConfigurationFile(path: String) =
        ClassLoader.getSystemClassLoader().getResource("com/nuecho/genesys/cli/$path")
}
