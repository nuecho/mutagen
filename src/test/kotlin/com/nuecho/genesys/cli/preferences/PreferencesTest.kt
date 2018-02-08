package com.nuecho.genesys.cli.preferences

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File

class PreferencesTest : StringSpec() {
    init {
        "loadEnvironment with no environment specified should return the environment named default" {
            val environmentUri = ClassLoader.getSystemClassLoader().getResource("preferences/environments.yml").toURI()
            val environmentFile = File(environmentUri)
            val environment = Preferences.loadEnvironment(environmentsFile = environmentFile)

            environment shouldBe EnvironmentTest.defaultTestEnvironment
        }
    }
}