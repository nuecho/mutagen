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
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertThrows
import java.io.File
import java.io.FileNotFoundException

private const val PASSWORD = "password!"

class PreferencesTest {

    @Test
    fun `loadEnvironment with no environment specified should return the environment named default`() {

        val environment = TestResources.loadEnvironments("environments.yml")[Preferences.DEFAULT_ENVIRONMENT]
        assertEquals(environment, EnvironmentTest.defaultTestEnvironment)
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

        objectMockk(Password).use {
            every { Password.promptForPassword() } returns PASSWORD

            val environment = loadEnvironment(
                environmentsFile = toPreferenceFile("environments_nopassword.yml")
            )
            assertEquals(environment.password, PASSWORD)
        }
    }
}
