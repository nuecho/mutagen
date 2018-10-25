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

package com.nuecho.mutagen.cli.commands

import com.nuecho.mutagen.cli.preferences.environment.Environment
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.services.Services.withService
import picocli.CommandLine

abstract class ConfigServerCommand : MutagenCliCommand() {
    @CommandLine.Option(
        names = ["--trust-insecure-certificate"],
        description = ["Don't validate the server's TLS certificate."]
    )
    private var trustInsecureCertificate = false

    @Suppress("UNCHECKED_CAST")
    internal fun <T> withEnvironmentConfService(function: (service: ConfService, environment: Environment) -> T): T {
        val environment = getMutagenCli().loadEnvironment(password)
        val service = ConfService(
            environment = environment,
            checkCertificate = !trustInsecureCertificate
        )
        return withService(service) { function(service, environment) }
    }
}
