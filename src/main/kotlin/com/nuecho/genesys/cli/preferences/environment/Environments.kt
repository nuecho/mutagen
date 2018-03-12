package com.nuecho.genesys.cli.preferences.environment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

class Environments : LinkedHashMap<String, Environment>() {
    companion object {
        fun load(environmentsFile: File): Environments {
            try {
                return mapper().readValue(environmentsFile, Environments::class.java)
            } catch (exception: Exception) {
                throw EnvironmentException("Cannot load environments file ($environmentsFile).", exception)
            }
        }

        internal fun load(serializedEnvironments: String): Environments {
            try {
                return mapper().readValue(serializedEnvironments, Environments::class.java)
            } catch (exception: Exception) {
                throw EnvironmentException("Cannot load environments.", exception)
            }
        }

        private fun mapper(): ObjectMapper {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModule(KotlinModule())
            return mapper
        }
    }

    fun saveToFile(file: File) = mapper().writeValue(file, this)
}

class EnvironmentException(message: String, cause: Throwable) : Exception(message, cause)
