package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging
import com.nuecho.genesys.cli.commands.config.Config
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.models.configuration.Person
import com.nuecho.genesys.cli.models.configuration.Skill
import com.nuecho.genesys.cli.models.configuration.import
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.defaultTenantDbid
import com.nuecho.genesys.cli.services.retrievePerson
import com.nuecho.genesys.cli.services.retrieveSkill
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

        private val PERSONS = "persons"
        private val SKILLS = "skills"

        fun importConfiguration(configuration: Configuration, service: ConfService) {

            Logging.info { "Beginning import." }

            val count = importSkills(configuration.skills, service) +
                    importPersons(configuration.persons, service)

            println("Completed. $count object(s) imported.")
        }

        internal fun importPersons(persons: Collection<Person>, service: ConfService): Int {
            var count = 0
            persons.forEach {
                var employeeId = it.employeeId
                var person = service.retrievePerson(employeeId)

                if (person != null) {
                    objectImportProgress(PERSONS, it.primaryKey, true)
                    return@forEach
                }

                person = CfgPerson(service)

                Logging.info { "Creating ${person.javaClass.simpleName} '${it.primaryKey}'." }

                person.import(it)
                save(applyTenant(person))
                objectImportProgress(PERSONS, it.primaryKey)
                count++
            }
            return count
        }

        internal fun importSkills(skills: Collection<Skill>, service: ConfService): Int {
            var count = 0

            skills.forEach {
                var name = it.name
                var skill = service.retrieveSkill(name)

                if (skill != null) {
                    objectImportProgress(SKILLS, it.primaryKey, true)
                    return@forEach
                }

                skill = CfgSkill(service)

                Logging.info { "Creating ${skill.javaClass.simpleName} '${it.primaryKey}'." }

                skill.import(it)
                save(applyTenant(skill))
                objectImportProgress(SKILLS, it.primaryKey)
                count++
            }
            return count
        }

        internal fun objectImportProgress(type: String, key: String, skip: Boolean = false) {
            val prefix = if (skip) "=" else "+"
            println("$prefix $type.$key")
        }

        internal fun save(cfgObject: CfgObject) = applyTenant(cfgObject).save()

        internal fun applyTenant(cfgObject: CfgObject): CfgObject =
            cfgObject.apply { setProperty("tenantDBID", cfgObject.configurationService.defaultTenantDbid) }
    }
}
