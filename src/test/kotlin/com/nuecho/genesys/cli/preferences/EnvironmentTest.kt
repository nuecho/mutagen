package com.nuecho.genesys.cli.preferences

import com.nuecho.genesys.cli.GenesysServices
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrowAny
import io.kotlintest.specs.StringSpec

class EnvironmentTest : StringSpec() {
    init {
        "omitting optional environment properties should give default values" {
            val environments = loadEnvironments("environments.yml")
            val host = "demosrv.nuecho.com"
            val user = "user"
            val password = "password"

            environments["default"] shouldBe Environment(
                host,
                GenesysServices.DEFAULT_SERVER_PORT,
                GenesysServices.DEFAULT_USE_TLS,
                user,
                password,
                GenesysServices.DEFAULT_APPLICATION_NAME)

            environments["override"] shouldBe Environment(
                host,
                2222,
                true,
                user,
                password,
                "myapp")
        }

        "invalid connection configuration file should throw an exception" {
            shouldThrowAny {
                loadEnvironments("invalid_connections.yml")
            }
        }
    }

    private fun loadEnvironments(path: String): Environments {
        return Environments.load(ClassLoader.getSystemClassLoader().getResource(path).readText())
    }
}