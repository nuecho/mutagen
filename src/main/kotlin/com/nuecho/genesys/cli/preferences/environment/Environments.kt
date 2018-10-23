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
