package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging
import com.nuecho.genesys.cli.commands.config.Config
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.models.configuration.Person
import com.nuecho.genesys.cli.models.configuration.import
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.defaultTenantDbid
import com.nuecho.genesys.cli.services.retrievePerson
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
            importPersons(configuration.persons, service)
            Logging.info { "Import completed." }
        }

        internal fun importPersons(persons: Collection<Person>, service: ConfService) {
            persons.forEach {
                var employeeId = it.employeeId
                var person = service.retrievePerson(employeeId)

                if (person != null) {
                    Logging.info { "CfgPerson '$employeeId' already exists, skipping." }
                    return
                }

                Logging.info { "Creating CfgPerson '$employeeId'." }
                person = CfgPerson(service)
                person.import(it)
                save(applyTenant(person))
            }
        }

        internal fun save(cfgObject: CfgObject) = applyTenant(cfgObject).save()

        internal fun applyTenant(cfgObject: CfgObject): CfgObject =
            cfgObject.apply { setProperty("tenantDBID", cfgObject.configurationService.defaultTenantDbid) }
    }
}
