package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.Logging.warn
import com.nuecho.genesys.cli.commands.config.Config
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName
import org.jgrapht.Graph
import org.jgrapht.alg.cycle.CycleDetector
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.traverse.TopologicalOrderIterator
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

            info { "Preparing import." }

            val configurationObjects = prepareImportOperation(configuration, service)

            info { "Beginning import." }

            val count = configurationObjects
                .map { importConfigurationObject(it, service) }
                .filter { it }
                .count()

            println("Completed. $count object(s) imported.")
        }

        internal fun prepareImportOperation(configuration: Configuration, service: ConfService):
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

        internal fun importConfigurationObject(
            configurationObject: ConfigurationObject,
            service: IConfService
        ): Boolean {
            val reference = configurationObject.reference
            val cfgObject = configurationObject.updateCfgObject(service)
            val type = cfgObject.objectType.toShortName()

            info { "Processing $type '$reference'." }
            val create = !cfgObject.isSaved
            save(cfgObject)
            objectImportProgress(type, reference, create)

            // TODO This should eventually return false if the object was identical and therefore not updated
            return true
        }

        private fun objectImportProgress(
            type: String,
            reference: ConfigurationObjectReference<*>,
            create: Boolean
        ) {
            val prefix = if (create) "+" else "~"
            println("$prefix $type => $reference")
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
