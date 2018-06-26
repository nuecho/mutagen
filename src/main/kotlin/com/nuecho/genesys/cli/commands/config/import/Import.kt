package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.Logging.warn
import com.nuecho.genesys.cli.commands.config.Config
import com.nuecho.genesys.cli.commands.config.import.ImportOperationType.CREATE
import com.nuecho.genesys.cli.commands.config.import.ImportOperationType.UPDATE
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.ConfServiceCache
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName
import org.jgrapht.Graph
import org.jgrapht.alg.cycle.CycleDetector
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.traverse.TopologicalOrderIterator
import picocli.CommandLine
import java.io.File

const val YES: String = "y"
const val NO: String = "n"

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

    @CommandLine.Option(
        names = ["--auto-confirm"],
        description = ["Skip interactive approval before applying."]
    )
    private var autoConfirm: Boolean = false

    override fun getGenesysCli() = config!!.getGenesysCli()

    override fun execute(): Int {
        val result = withEnvironmentConfService {
            ConfServiceCache.populateCache(it)
            val configuration = defaultJsonObjectMapper().readValue(inputFile, Configuration::class.java)
            importConfiguration(configuration, it, autoConfirm)
        }
        return if (result) 0 else 1
    }

    companion object {
        fun importConfiguration(configuration: Configuration, service: ConfService, autoConfirm: Boolean): Boolean {
            info { "Preparing import." }

            val configurationObjects = extractTopologicalSequence(configuration, service)

            if (!autoConfirm) {
                printPlan(configurationObjects, service)

                if (!confirm()) {
                    println("Import cancelled.")
                    return false
                }
            }

            info { "Beginning import." }

            val count = configurationObjects
                .map { importConfigurationObject(it, service) }
                .filter { it }
                .count()

            println("Completed. $count object(s) imported.")
            return true
        }

        internal fun extractTopologicalSequence(configuration: Configuration, service: ConfService):
                List<ConfigurationObject> {
            val objectDependencyGraph: Graph<ConfigurationObject, DefaultEdge> =
                DefaultDirectedGraph(DefaultEdge::class.java)

            val configurationObjectsByReference = configuration.toMapByReference()

            // We need to add graph vertex before we connect them together
            configurationObjectsByReference.values.forEach { objectDependencyGraph.addVertex(it) }

            var foundMissingObject = false
            for (configurationObject in configurationObjectsByReference.values) {
                for (dependency in configurationObject.getReferences()) {
                    // If the dependency is part of the configuration we are trying to import, add it to the
                    // dependency graph. Else, make sure the dependency actually exists on the configuration server.
                    if (configurationObjectsByReference.containsKey(dependency)) {
                        objectDependencyGraph.addEdge(configurationObjectsByReference[dependency], configurationObject)
                    } else if (service.retrieveObject(dependency) == null) {
                        warn {
                            "Cannot find ${dependency.getCfgObjectType().toShortName()} '$dependency' " +
                                    "(referenced by '${configurationObject.reference}')"
                        }
                        foundMissingObject = true
                    }
                }
            }

            if (foundMissingObject) throw UnresolvedConfigurationObjectReferenceException()
            if (CycleDetector(objectDependencyGraph).detectCycles()) throw ConfigurationObjectCycleException()

            return TopologicalOrderIterator(objectDependencyGraph).asSequence().toList()
        }

        internal fun printPlan(configurationObjects: Collection<ConfigurationObject>, service: ConfService) {
            println("The following changes are going to be applied:")
            configurationObjects.forEach {
                // Those objects will be fetched again later... A better modelization of the plan & operations
                // could prevent us from fetching the objects multiple times.
                val operationType = if (service.retrieveObject(it.reference) == null) CREATE else UPDATE
                printObjectImportStatus(it.reference, operationType)
            }
            println()
        }

        internal fun confirm(): Boolean {
            var confirmed: String
            do {
                print("Please confirm [$YES|$NO]: ")
                confirmed = readLine()?.toLowerCase() ?: ""
            } while (confirmed != YES && confirmed != NO)

            return confirmed == YES
        }

        internal fun importConfigurationObject(
            configurationObject: ConfigurationObject,
            service: IConfService
        ): Boolean {
            val reference = configurationObject.reference
            val cfgObject = configurationObject.updateCfgObject(service)
            val type = cfgObject.objectType.toShortName()

            info { "Processing $type '$reference'." }
            val status = if (cfgObject.isSaved) UPDATE else CREATE
            save(cfgObject)
            printObjectImportStatus(reference, status)

            // TODO This should eventually return false if the object was identical and therefore not updated
            return true
        }

        private fun printObjectImportStatus(
            reference: ConfigurationObjectReference<*>,
            status: ImportOperationType
        ) {
            println("$status ${reference.getCfgObjectType().toShortName()} => $reference")
        }

        internal fun save(cfgObject: CfgObject) = cfgObject.save()
    }
}

class UnresolvedConfigurationObjectReferenceException : Exception(
    "Cannot import configuration: some configuration object dependency could not be found."
)

class ConfigurationObjectCycleException : Exception(
    "Cannot import configuration: dependency cycles is not yet supported."
)

enum class ImportOperationType(val symbol: String) {
    CREATE("+"), UPDATE("~"), SKIP("=");

    override fun toString() = symbol
}
