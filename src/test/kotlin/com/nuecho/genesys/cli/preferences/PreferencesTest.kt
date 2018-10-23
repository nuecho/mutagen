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
import com.nuecho.genesys.cli.TestResources
import com.nuecho.genesys.cli.TestResources.toPreferenceFile
import com.nuecho.genesys.cli.preferences.Preferences.CUSTOM_HOME_VARIABLE
import com.nuecho.genesys.cli.preferences.Preferences.HOME_VARIABLE
import com.nuecho.genesys.cli.preferences.Preferences.PREFERENCES_DIRECTORY_NAME
import com.nuecho.genesys.cli.preferences.Preferences.USER_PROFILE_VARIABLE
import com.nuecho.genesys.cli.preferences.Preferences.WORKING_DIRECTORY_VARIABLE
import com.nuecho.genesys.cli.preferences.Preferences.loadEnvironment
import com.nuecho.genesys.cli.preferences.environment.EnvironmentTest
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileNotFoundException

private const val PASSWORD = "password!"

class PreferencesTest {

    @Test
    fun `loadEnvironment with no environment specified should return the environment named default`() {

        val environment = TestResources.loadEnvironments("environments.yml")[Preferences.DEFAULT_ENVIRONMENT]
        assertThat(environment, equalTo(EnvironmentTest.defaultTestEnvironment))
    }

    @Test
    fun `findEnvironmentFile should look in all predefined locations`() {
        val home = File(ClassLoader.getSystemClassLoader().getResource(".mutagen").toURI()).parent
        val devNull = "/dev/null"

        assertThrows(FileNotFoundException::class.java) {
            Preferences.findEnvironmentFile(emptyMap())
        }

        assertThrows(FileNotFoundException::class.java) {
            Preferences.findEnvironmentFile(mapOf("ET_PHONE_HOME" to home))
        }

        Preferences.findEnvironmentFile(mapOf(WORKING_DIRECTORY_VARIABLE to "$home/$PREFERENCES_DIRECTORY_NAME"))
        Preferences.findEnvironmentFile(mapOf(CUSTOM_HOME_VARIABLE to "$home/$PREFERENCES_DIRECTORY_NAME"))
        Preferences.findEnvironmentFile(mapOf(HOME_VARIABLE to home))
        Preferences.findEnvironmentFile(mapOf(USER_PROFILE_VARIABLE to home))

        Preferences.findEnvironmentFile(
            mapOf(
                WORKING_DIRECTORY_VARIABLE to devNull,
                CUSTOM_HOME_VARIABLE to devNull,
                HOME_VARIABLE to devNull,
                USER_PROFILE_VARIABLE to home
            )
        )
    }

    @Test
    fun `missing password should prompt for one`() {
        objectMockk(Console).use {
            every { Console.promptForPassword() } returns SecurePassword(PASSWORD.toCharArray())

            val environment = loadEnvironment(
                environmentsFile = toPreferenceFile("environments_nopassword.yml")
            )

            assertThat(environment.password!!.value, equalTo(PASSWORD))
        }
    }
}
