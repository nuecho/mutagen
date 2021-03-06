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

package com.nuecho.mutagen.cli.preferences.environment

import com.nuecho.mutagen.cli.TestResources.loadEnvironments
import com.nuecho.mutagen.cli.services.GenesysServices.DEFAULT_APPLICATION_NAME
import com.nuecho.mutagen.cli.services.GenesysServices.DEFAULT_SERVER_PORT
import com.nuecho.mutagen.cli.services.GenesysServices.DEFAULT_USE_TLS
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.File

class EnvironmentTest {
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

    @Test
    fun `omitting optional selectedEnvironment properties should give default values`() {
        val environments = loadEnvironments("environments.yml")

        assertThat(environments["default"], equalTo(defaultTestEnvironment))
    }

    @Test
    fun `providing optional selectedEnvironment properties should override default values`() {
        val environments = loadEnvironments("environments.yml")

        assertThat(environments["override"], equalTo(overrideTestEnvironment))
    }

    @Test
    fun `loading an invalid environments file should throw an exception`() {
        assertThrows(EnvironmentException::class.java) {
            loadEnvironments("invalid_environments.yml")
        }
    }

    @Test
    fun `loading a non-existing environments file should throw an exception`() {
        assertThrows(EnvironmentException::class.java) {
            Environments.load(File(""))
        }
    }
}
