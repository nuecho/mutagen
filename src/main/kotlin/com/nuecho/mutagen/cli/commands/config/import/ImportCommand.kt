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

package com.nuecho.mutagen.cli.commands.config.import

import com.nuecho.mutagen.cli.Console.confirm
import com.nuecho.mutagen.cli.Logging.info
import com.nuecho.mutagen.cli.PASSWORD_FROM_STDIN
import com.nuecho.mutagen.cli.commands.ConfigServerCommand
import com.nuecho.mutagen.cli.commands.config.Config
import com.nuecho.mutagen.cli.commands.config.Validator
import com.nuecho.mutagen.cli.commands.config.import.Import.importConfiguration
import com.nuecho.mutagen.cli.commands.config.import.operation.ImportOperationType.CREATE
import com.nuecho.mutagen.cli.commands.config.import.operation.ImportOperationType.SKIP
import com.nuecho.mutagen.cli.commands.config.import.operation.ImportOperationType.UPDATE
import com.nuecho.mutagen.cli.commands.config.import.operation.ImportPlan
import com.nuecho.mutagen.cli.core.defaultJsonObjectMapper
import com.nuecho.mutagen.cli.models.configuration.Configuration
import com.nuecho.mutagen.cli.pluralize
import com.nuecho.mutagen.cli.services.ConfService
import picocli.CommandLine
import java.io.File

private const val AUTO_CONFIRM = "--auto-confirm"

@CommandLine.Command(
    name = "import",
    description = ["[INCUBATION] Import configuration objects."]
)
class ImportCommand : ConfigServerCommand() {
    @CommandLine.ParentCommand
    private var config: Config? = null

    @CommandLine.Parameters(
        arity = "1",
        index = "0",
        paramLabel = "inputFile",
        description = ["Input configuration file."]
    )
    private var inputFile: File? = null

    @CommandLine.Option(
        names = [AUTO_CONFIRM],
        description = ["Skip interactive approval before applying."]
    )
    private var autoConfirm: Boolean = false

    override fun getMutagenCli() = config!!.getMutagenCli()

    override fun execute(): Int {
        if (getMutagenCli().readPasswordFromStdin && !autoConfirm)
            throw ConfigImportException("$AUTO_CONFIRM must be specified in conjunction with $PASSWORD_FROM_STDIN.")

        val result = withEnvironmentConfService { service: ConfService, _ ->
            service.prefetchConfigurationObjects()

            val configurationString = Configuration.interpolateVariables(
                inputFile!!.readText(),
                System.getenv().toMap()
            )

            val configuration = defaultJsonObjectMapper().readValue(configurationString, Configuration::class.java)
            importConfiguration(configuration, service, autoConfirm)
        }
        return if (result) 0 else 1
    }
}

object Import {
    internal fun importConfiguration(
        configuration: Configuration,
        service: ConfService,
        autoConfirm: Boolean
    ): Boolean {
        info { "Preparing import." }

        Validator(configuration, service).validateConfiguration()

        val plan = ImportPlan(configuration, service)

        if (!autoConfirm) {
            plan.print()

            if (!confirm()) {
                println("Import cancelled.")
                return false
            }
        }

        info { "Beginning import." }
        val count = plan.apply()

        println(
            "Completed. ${count[CREATE]} ${"object".pluralize(count[CREATE]!!)} created. " +
                    "${count[UPDATE]} ${"object".pluralize(count[UPDATE]!!)} updated. " +
                    "${count[SKIP]} ${"object".pluralize(count[SKIP]!!)} skipped."
        )

        return true
    }
}

class ConfigImportException(message: String, cause: Throwable? = null) : Exception(message, cause)
