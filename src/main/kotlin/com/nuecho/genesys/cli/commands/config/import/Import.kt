package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging
import com.nuecho.genesys.cli.commands.config.Config
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.toShortName
import picocli.CommandLine
import java.io.File

@CommandLine.Command(
    name = "import",
    description = ["[INCUBATION] Import configuration objects."]
)
class Import : GenesysCliCommand() {
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

    override fun execute() {
        withEnvironmentConfService {
            val configuration = defaultJsonObjectMapper().readValue(inputFile, Configuration::class.java)
            importConfiguration(configuration, it)
        }
    }

    companion object {
        fun importConfiguration(configuration: Configuration, service: ConfService) {

            Logging.info { "Beginning import." }

            val count = intArrayOf(
                importConfigurationObjects(configuration.actionCodes, service),
                importConfigurationObjects(configuration.agentGroups, service),
                importConfigurationObjects(configuration.dns, service),
                importConfigurationObjects(configuration.enumerators, service),
                importConfigurationObjects(configuration.gvpCustomers, service),
                importConfigurationObjects(configuration.gvpResellers, service),
                importConfigurationObjects(configuration.skills, service),
                importConfigurationObjects(configuration.roles, service),
                importConfigurationObjects(configuration.persons, service),
                importConfigurationObjects(configuration.physicalSwitches, service),
                importConfigurationObjects(configuration.scripts, service),
                importConfigurationObjects(configuration.switches, service),
                importConfigurationObjects(configuration.tenants, service),
                importConfigurationObjects(configuration.transactions, service)
            ).sum()

            println("Completed. $count object(s) imported.")
        }

        internal fun importConfigurationObjects(
            objects: Collection<ConfigurationObject>,
            service: IConfService
        ): Int {
            var count = 0

            objects.forEach {
                val primaryKey = it.reference
                val (status, cfgObject) = it.updateCfgObject(service)
                val type = cfgObject.objectType.toShortName()

                if (status != CREATED) {
                    objectImportProgress(type, primaryKey, true)
                    return@forEach
                }

                Logging.info { "Creating $type '$primaryKey'." }
                save(cfgObject)
                objectImportProgress(type, primaryKey)
                count++
            }

            return count
        }

        private fun objectImportProgress(
            type: String,
            reference: ConfigurationObjectReference<*>,
            skip: Boolean = false
        ) {
            val prefix = if (skip) "=" else "+"
            println("$prefix $type => $reference")
        }

        internal fun save(cfgObject: CfgObject) = cfgObject.save()
    }
}
