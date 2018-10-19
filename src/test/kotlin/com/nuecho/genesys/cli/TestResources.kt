/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
