package com.nuecho.genesys.cli.preferences.environment

import com.nuecho.genesys.cli.core.defaultYamlObjectMapper
import java.io.File

class Environments : LinkedHashMap<String, Environment>() {
    companion object {
        fun load(environmentsFile: File): Environments {
            try {
                return defaultYamlObjectMapper().readValue(environmentsFile, Environments::class.java)
            } catch (exception: Exception) {
                throw EnvironmentException("Cannot load environments file ($environmentsFile).", exception)
            }
        }

        internal fun load(serializedEnvironments: String): Environments {
            try {
                return defaultYamlObjectMapper().readValue(serializedEnvironments, Environments::class.java)
            } catch (exception: Exception) {
                throw EnvironmentException("Cannot load environments.", exception)
            }
        }
    }

    fun saveToFile(file: File) = defaultYamlObjectMapper().writeValue(file, this)
}

class EnvironmentException(message: String, cause: Throwable) : Exception(message, cause)
