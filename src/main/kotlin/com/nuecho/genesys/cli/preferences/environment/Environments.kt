package com.nuecho.genesys.cli.preferences.environment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

class Environments : LinkedHashMap<String, Environment>() {
    companion object {
        fun load(string: String): Environments {
            try {
                return mapper().readValue(string, Environments::class.java)
            } catch (exception: Exception) {
                throw EnvironmentException("Cannot load environments.", exception)
            }
        }

        fun load(environmentsFile: File): Environments {
            try {
                return mapper().readValue(environmentsFile, Environments::class.java)
            } catch (exception: Exception) {
                throw EnvironmentException("Cannot load environments file ($environmentsFile).", exception)
            }
        }

        private fun mapper(): ObjectMapper {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModule(KotlinModule())
            return mapper
        }
    }
}

class EnvironmentException(message: String, cause: Throwable) : Exception(message, cause)
