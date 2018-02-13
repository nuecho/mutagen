package com.nuecho.genesys.cli.preferences

import com.nuecho.genesys.cli.GenesysServices.DEFAULT_APPLICATION_NAME
import com.nuecho.genesys.cli.GenesysServices.DEFAULT_SERVER_PORT
import com.nuecho.genesys.cli.GenesysServices.DEFAULT_USE_TLS
import com.nuecho.genesys.cli.TestResources.loadEnvironments
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrowAny
import io.kotlintest.specs.StringSpec

class EnvironmentTest : StringSpec() {
    companion object {
        val defaultTestEnvironment = Environment(
            host = "localhost",
            port = DEFAULT_SERVER_PORT,
            tls = DEFAULT_USE_TLS,
            user = "default",
            password = "password",
            application = DEFAULT_APPLICATION_NAME
        )

        val overrideTestEnvironment = Environment(
            host = "demosrv.nuecho.com",
            port = 2222,
            tls = true,
            user = "user",
            password = "password",
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

        "invalid connection configuration file should throw an exception" {
            shouldThrowAny {
                loadEnvironments("invalid_connections.yml")
            }
        }
    }
}
