package com.nuecho.genesys.cli.preferences

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File

class PreferencesTest : StringSpec() {
    init {
        "loadEnvironment with no environment specified should return the environment named default" {
            val environmentFile = File(ClassLoader.getSystemClassLoader().getResource("environments.yml").toURI())
            val environment = Preferences.loadEnvironment(environmentsFile = environmentFile)

            environment shouldBe EnvironmentTest.defaultTestEnvironment
        }
    }
}