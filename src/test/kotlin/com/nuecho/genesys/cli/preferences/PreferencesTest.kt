package com.nuecho.genesys.cli.preferences

import com.nuecho.genesys.cli.preferences.environment.EnvironmentTest
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.StringSpec
import java.io.File

class PreferencesTest : StringSpec() {
    init {
        "loadEnvironment with no environment specified should return the environment named default" {
            val environment = Preferences.loadEnvironment(environmentsFile = environmentFile())

            environment shouldBe EnvironmentTest.defaultTestEnvironment
        }

        "loadEnvironment with non-existing environment name should fail" {
            shouldThrow<IllegalArgumentException> {
                Preferences.loadEnvironment(environment = "missing", environmentsFile = environmentFile())
            }
        }
    }

    private fun environmentFile(): File {
        val environmentUri = ClassLoader.getSystemClassLoader().getResource("preferences/environments.yml").toURI()
        return File(environmentUri)
    }
}
