package com.nuecho.genesys.cli.preferences

import com.nuecho.genesys.cli.TestResources
import com.nuecho.genesys.cli.TestResources.toFile
import com.nuecho.genesys.cli.preferences.Preferences.loadEnvironment
import com.nuecho.genesys.cli.preferences.environment.EnvironmentTest
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import java.io.File

class PreferencesTest : StringSpec() {
    private val MOCK_PASSWORD = "password!"

    init {
        "loadEnvironment with no environment specified should return the environment named default" {

            val environment = TestResources.loadEnvironments("environments.yml")[Preferences.DEFAULT_ENVIRONMENT]
            environment shouldBe EnvironmentTest.defaultTestEnvironment
        }

        "missing password should prompt for one" {

            objectMockk(Preferences).use {
                every { Preferences.promptForPassword() } returns MOCK_PASSWORD

                val environment = loadEnvironment(
                    environmentsFile = toFile("environments_nopassword.yml")
                )
                environment.password shouldBe MOCK_PASSWORD
            }
        }
    }

    private fun environmentFile(): File {
        val environmentUri = ClassLoader.getSystemClassLoader().getResource("preferences/environments.yml").toURI()
        return File(environmentUri)
    }
}
