package com.nuecho.genesys.cli.preferences

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

class Environments : LinkedHashMap<String, Environment>() {
    companion object {
        fun load(string: String): Environments {
            return mapper().readValue(string, Environments::class.java)
        }

        fun load(file: File): Environments {
            return mapper().readValue(file, Environments::class.java)
        }

        private fun mapper(): ObjectMapper {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModule(KotlinModule())
            return mapper
        }
    }
}