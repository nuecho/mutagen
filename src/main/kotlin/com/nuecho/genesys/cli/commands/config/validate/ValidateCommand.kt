package com.nuecho.genesys.cli.commands.config.validate

import com.nuecho.genesys.cli.commands.ConfigServerCommand
import com.nuecho.genesys.cli.commands.config.Config
import com.nuecho.genesys.cli.commands.config.Validator
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
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

    override fun getGenesysCli() = config!!.getGenesysCli()

    override fun execute(): Int {
        withEnvironmentConfService { service: ConfService, _ ->
            ConfigurationObjectRepository.prefetchConfigurationObjects(service)

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
