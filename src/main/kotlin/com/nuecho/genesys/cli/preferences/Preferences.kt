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

package com.nuecho.genesys.cli.preferences

import com.nuecho.genesys.cli.Console
import com.nuecho.genesys.cli.Logging.debug
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.preferences.environment.Environments
import java.io.File
import java.io.FileNotFoundException

object Preferences {
    const val DEFAULT_ENVIRONMENT = "default"

    const val WORKING_DIRECTORY_VARIABLE = "user.dir"
    const val CUSTOM_HOME_VARIABLE = "MUTAGEN_HOME"
    const val USER_PROFILE_VARIABLE = "USERPROFILE"
    const val HOME_VARIABLE = "HOME"
    const val PREFERENCES_DIRECTORY_NAME = ".mutagen"
    private const val ENVIRONMENTS_FILENAME = "environments.yml"

    fun loadEnvironment(
        environmentName: String = DEFAULT_ENVIRONMENT,
        environmentsFile: File? = null,
        password: SecurePassword? = null
    ): Environment {
        val environmentVariables = System.getenv().toMutableMap()
        environmentVariables[WORKING_DIRECTORY_VARIABLE] = System.getProperty(WORKING_DIRECTORY_VARIABLE)

        val effectiveEnvironmentsFile = environmentsFile ?: findEnvironmentFile(environmentVariables)
        info { "Loading environment file ($effectiveEnvironmentsFile)" }

        val environment = Environments.load(effectiveEnvironmentsFile)[environmentName]
                ?: throw IllegalArgumentException("Environment ($environmentName) does not exist")

        if (environment.password == null) {
            environment.password = if (password != null) password else Console.promptForPassword()
        }

        return environment
    }

    fun findEnvironmentFile(): File {
        val environmentVariables = System.getenv().toMutableMap()
        environmentVariables[WORKING_DIRECTORY_VARIABLE] = System.getProperty(WORKING_DIRECTORY_VARIABLE)

        return findEnvironmentFile(environmentVariables)
    }

    internal fun findEnvironmentFile(environmentVariables: Map<String?, String?>): File {
        mapOf(
            WORKING_DIRECTORY_VARIABLE to false,
            CUSTOM_HOME_VARIABLE to false,
            HOME_VARIABLE to true,
            USER_PROFILE_VARIABLE to true
        ).forEach { (environmentPathVariable, appendPreferenceDirectory) ->
            val environmentPath = environmentVariables[environmentPathVariable]

            if (environmentPath == null) {
                debug { "$environmentPathVariable is not defined." }
                return@forEach
            }

            val environmentDirectory =
                if (appendPreferenceDirectory) File(environmentPath, PREFERENCES_DIRECTORY_NAME)
                else File(environmentPath)

            val environmentFile = File(environmentDirectory, ENVIRONMENTS_FILENAME)

            if (environmentFile.exists()) return environmentFile
            debug { "No environment file found in $environmentPathVariable (${environmentFile.parent})" }
        }

        throw FileNotFoundException("Cannot find environment file.")
    }
}
