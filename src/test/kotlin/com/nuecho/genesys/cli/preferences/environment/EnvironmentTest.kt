package com.nuecho.genesys.cli.preferences.environment

import com.nuecho.genesys.cli.TestResources.loadEnvironments
import com.nuecho.genesys.cli.services.GenesysServices.DEFAULT_APPLICATION_NAME
import com.nuecho.genesys.cli.services.GenesysServices.DEFAULT_SERVER_PORT
import com.nuecho.genesys.cli.services.GenesysServices.DEFAULT_USE_TLS
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.StringSpec
import java.io.File

class EnvironmentTest : StringSpec() {
    companion object {
        val defaultTestEnvironment = Environment(
            host = "localhost",
            port = DEFAULT_SERVER_PORT,
            tls = DEFAULT_USE_TLS,
            user = "default",
            rawPassword = "password",
            application = DEFAULT_APPLICATION_NAME
        )

        val overrideTestEnvironment = Environment(
            host = "demosrv.nuecho.com",
            port = 2222,
            tls = true,
            user = "user",
            rawPassword = "password",
            application = "myapp"
        )
    }

    init {
        "omitting optional selectedEnvironment properties should give default values" {
            val environments = loadEnvironments("environments.yml")

            environments["default"] shouldBe defaultTestEnvironment
        }

        "providing optional selectedEnvironment properties should override default values" {
            val environments = loadEnvironments("environments.yml")

            environments["override"] shouldBe overrideTestEnvironment
        }

        "loading an invalid environments file should throw an exception" {
            shouldThrow<EnvironmentException> {
                loadEnvironments("invalid_environments.yml")
            }
        }

        "loading a non-existing environments file should throw an exception" {
            shouldThrow<EnvironmentException> {
                Environments.load(File(""))
            }
        }
    }
}
