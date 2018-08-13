package com.nuecho.genesys.cli.commands.config.import

import com.nuecho.genesys.cli.Console.confirm
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.PASSWORD_FROM_STDIN
import com.nuecho.genesys.cli.commands.ConfigServerCommand
import com.nuecho.genesys.cli.commands.config.Config
import com.nuecho.genesys.cli.commands.config.Validator
import com.nuecho.genesys.cli.commands.config.import.Import.importConfiguration
import com.nuecho.genesys.cli.commands.config.import.operation.ImportOperationType.CREATE
import com.nuecho.genesys.cli.commands.config.import.operation.ImportOperationType.SKIP
import com.nuecho.genesys.cli.commands.config.import.operation.ImportOperationType.UPDATE
import com.nuecho.genesys.cli.commands.config.import.operation.ImportPlan
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.pluralize
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
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

    override fun getGenesysCli() = config!!.getGenesysCli()

    override fun execute(): Int {
        if (getGenesysCli().readPasswordFromStdin && !autoConfirm)
            throw ConfigImportException("$AUTO_CONFIRM must be specified in conjunction with $PASSWORD_FROM_STDIN.")

        val result = withEnvironmentConfService { service: ConfService, _ ->
            ConfigurationObjectRepository.prefetchConfigurationObjects(service)

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
