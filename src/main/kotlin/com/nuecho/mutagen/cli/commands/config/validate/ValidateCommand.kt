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

package com.nuecho.mutagen.cli.commands.config.validate

import com.nuecho.mutagen.cli.commands.ConfigServerCommand
import com.nuecho.mutagen.cli.commands.config.Config
import com.nuecho.mutagen.cli.commands.config.Validator
import com.nuecho.mutagen.cli.core.defaultJsonObjectMapper
import com.nuecho.mutagen.cli.models.configuration.Configuration
import com.nuecho.mutagen.cli.services.ConfService
import picocli.CommandLine
import java.io.File

@CommandLine.Command(
    name = "validate",
    description = ["Validate the configuration objects against the Configuration Server."]
)
class ValidateCommand : ConfigServerCommand() {
    @CommandLine.ParentCommand
    private var config: Config? = null

    @CommandLine.Parameters(
        arity = "1",
        index = "0",
        paramLabel = "inputFile",
        description = ["Input configuration file."]
    )
    private var inputFile: File? = null

    override fun getMutagenCli() = config!!.getMutagenCli()

    override fun execute(): Int {
        withEnvironmentConfService { service: ConfService, _ ->
            service.prefetchConfigurationObjects()

            val configuration = defaultJsonObjectMapper().readValue(
                inputFile!!.readText(),
                Configuration::class.java
            )
            Validator(configuration, service).validateConfiguration()

            println("Validation succeeded.")
        }

        return 0
    }
}
