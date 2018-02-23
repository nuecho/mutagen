package com.nuecho.genesys.cli.preferences

import com.nuecho.genesys.cli.TestResources
import com.nuecho.genesys.cli.TestResources.toPreferenceFile
import com.nuecho.genesys.cli.preferences.Preferences.CUSTOM_HOME_VARIABLE
import com.nuecho.genesys.cli.preferences.Preferences.HOME_VARIABLE
import com.nuecho.genesys.cli.preferences.Preferences.PREFERENCES_DIRECTORY_NAME
import com.nuecho.genesys.cli.preferences.Preferences.USER_PROFILE_VARIABLE
import com.nuecho.genesys.cli.preferences.Preferences.WORKING_DIRECTORY_VARIABLE
import com.nuecho.genesys.cli.preferences.Preferences.loadEnvironment
import com.nuecho.genesys.cli.preferences.environment.EnvironmentTest
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import java.io.File
import java.io.FileNotFoundException

private const val PASSWORD = "password!"

class PreferencesTest : StringSpec() {

    init {
        "loadEnvironment with no environment specified should return the environment named default" {

            val environment = TestResources.loadEnvironments("environments.yml")[Preferences.DEFAULT_ENVIRONMENT]
            environment shouldBe EnvironmentTest.defaultTestEnvironment
        }

        "findEnvironmentFile should look in all predefined locations" {
            val home = File(ClassLoader.getSystemClassLoader().getResource(".mutagen").toURI()).parent
            val devNull = "/dev/null"

            shouldThrow<FileNotFoundException> {
                Preferences.findEnvironmentFile(emptyMap())
            }

            shouldThrow<FileNotFoundException> {
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

        "missing password should prompt for one" {

            objectMockk(Preferences).use {
                every { Preferences.promptForPassword() } returns PASSWORD

                val environment = loadEnvironment(
                    environmentsFile = toPreferenceFile("environments_nopassword.yml")
                )
                environment.password shouldBe PASSWORD
            }
        }
    }
}
