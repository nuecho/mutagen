package com.nuecho.genesys.cli.commands.config.import

import com.nuecho.genesys.cli.Console.confirm
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.commands.ConfigServerCommand
import com.nuecho.genesys.cli.commands.config.Config
import com.nuecho.genesys.cli.commands.config.import.Import.importConfiguration
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.commands.config.import.operation.ImportPlan
import com.nuecho.genesys.cli.commands.config.import.operation.MissingDependencies
import com.nuecho.genesys.cli.commands.config.import.operation.MissingProperties
import com.nuecho.genesys.cli.commands.config.import.operation.PRINT_MARGIN
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.toShortName
import picocli.CommandLine
import java.io.File

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
        names = ["--auto-confirm"],
        description = ["Skip interactive approval before applying."]
    )
    private var autoConfirm: Boolean = false

    override fun getGenesysCli() = config!!.getGenesysCli()

    override fun execute(): Int {
        val result = withEnvironmentConfService { service: ConfService, _: Environment ->
            ConfigurationObjectRepository.prefetchConfigurationObjects(service)

            val configurationString = inputFile!!.readText()
            Configuration.interpolateVariables(configurationString, System.getenv().toMap())

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

        val plan = ImportPlan(configuration, service)

        if (plan.missingProperties.isNotEmpty())
            throw MandatoryPropertiesNotSetException(plan.missingProperties)
        if (plan.missingDependencies.isNotEmpty())
            throw UnresolvedConfigurationObjectReferenceException(plan.missingDependencies)

        if (!autoConfirm) {
            plan.print()

            if (!confirm()) {
                println("Import cancelled.")
                return false
            }
        }

        info { "Beginning import." }
        val count = plan.apply()

        println("Completed. $count object(s) imported.")
        return true
    }
}

class MandatoryPropertiesNotSetException(misses: List<MissingProperties>) : Exception(
    "Cannot import configuration: some configuration objects' mandatory properties for creation are not set.\n" +
            misses.joinToString(separator = "\n$PRINT_MARGIN", prefix = PRINT_MARGIN, transform = ::format)
) {
    companion object {
        fun format(missingProperties: MissingProperties) = with(missingProperties) {
            "Properties [${properties.joinToString()}] " +
                    "not set in ${configurationObject.reference.getCfgObjectType().toShortName()} " +
                    "[${configurationObject.reference}]."
        }
    }
}

class UnresolvedConfigurationObjectReferenceException(misses: List<MissingDependencies>) : Exception(
    "Cannot import configuration: some configuration objects' dependencies could not be found.\n" +
            misses.joinToString(separator = "\n", transform = ::format)
) {
    companion object {
        fun format(missingDependencies: MissingDependencies) =
            missingDependencies.dependencies.joinToString(
                prefix = PRINT_MARGIN,
                separator = "\n$PRINT_MARGIN",
                transform = { format(missingDependencies.configurationObject, it) }
            )

        private fun format(source: ConfigurationObject, dependency: ConfigurationObjectReference<*>) =
            "Cannot find ${dependency.getCfgObjectType().toShortName()} [$dependency] " +
                    "(referenced by ${source.reference.getCfgObjectType().toShortName()} [${source.reference}])."
    }
}
