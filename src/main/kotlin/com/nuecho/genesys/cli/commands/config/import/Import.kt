package com.nuecho.genesys.cli.commands.config.import

import com.nuecho.genesys.cli.ConfigServerCommand
import com.nuecho.genesys.cli.Console.confirm
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.commands.config.Config
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.models.ImportPlan
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import picocli.CommandLine
import java.io.File

@CommandLine.Command(
    name = "import",
    description = ["[INCUBATION] Import configuration objects."]
)
class Import : ConfigServerCommand() {
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
        val result = withEnvironmentConfService {
            ConfigurationObjectRepository.prefetchConfigurationObjects(it)
            val configuration = defaultJsonObjectMapper().readValue(inputFile, Configuration::class.java)
            importConfiguration(configuration, it, autoConfirm)
        }
        return if (result) 0 else 1
    }

    companion object {
        fun importConfiguration(configuration: Configuration, service: ConfService, autoConfirm: Boolean): Boolean {
            info { "Preparing import." }

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

            println("Completed. $count object(s) imported.")
            return true
        }
    }
}
