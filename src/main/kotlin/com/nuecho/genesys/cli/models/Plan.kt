package com.nuecho.genesys.cli.models

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.nuecho.genesys.cli.Logging
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

class Plan(configuration: Configuration, service: ConfService) {

    val configurationObjects = extractTopologicalSequence(configuration, service)
    val service = service

    fun print() =
        printPlan(configurationObjects, service)

    fun apply() =
        configurationObjects
            .map { importConfigurationObject(it, service) }
            .filter { it }
            .count()

    companion object {
        @Suppress("ThrowsCount", "ComplexMethod")
        internal fun extractTopologicalSequence(configuration: Configuration, service: ConfService):
                List<ConfigurationObject> {
            val objectDependencyGraph: Graph<ConfigurationObject, DefaultEdge> =
                DefaultDirectedGraph(DefaultEdge::class.java)

            val configurationObjectsByReference = configuration.toMapByReference()

            // We need to add graph vertex before we connect them together
            configurationObjectsByReference.values.forEach { objectDependencyGraph.addVertex(it) }

            var hasMissingObject = false
            var hasMissingMandatoryProperties = false

            for (configurationObject in configurationObjectsByReference.values) {

                hasMissingMandatoryProperties = checkMandatoryProperties(
                    configurationObject,
                    service,
                    hasMissingMandatoryProperties
                )

                for (dependency in configurationObject.getReferences()) {
                    // If the dependency is part of the configuration we are trying to import, add it to the
                    // dependency graph. Else, make sure the dependency actually exists on the configuration server.
                    if (configurationObjectsByReference.containsKey(dependency)) {
                        objectDependencyGraph.addEdge(configurationObjectsByReference[dependency], configurationObject)
                    } else if (service.retrieveObject(dependency) == null) {
                        Logging.warn {
                            "Cannot find ${dependency.getCfgObjectType().toShortName()} '$dependency' " +
                                    "(referenced by '${configurationObject.reference}')"
                        }
                        hasMissingObject = true
                    }
                }
            }

            if (hasMissingObject) throw UnresolvedConfigurationObjectReferenceException()
            if (CycleDetector(objectDependencyGraph).detectCycles()) throw ConfigurationObjectCycleException()
            if (hasMissingMandatoryProperties) throw MandatoryPropertiesNotSetException()

            return TopologicalOrderIterator(objectDependencyGraph).asSequence().toList()
        }

        private fun checkMandatoryProperties(
            configurationObject: ConfigurationObject,
            service: ConfService,
            hasMissingProperties: Boolean
        ): Boolean {

            if (service.retrieveObject(configurationObject.reference) == null) {
                val missingProperties = configurationObject.checkMandatoryProperties()
                if (missingProperties.isNotEmpty()) {
                    Logging.warn {
                        "Mandatory properties $missingProperties not set in " +
                                "${configurationObject.reference.getCfgObjectType().toShortName()} " +
                                "'${configurationObject.reference}'"
                    }
                    return true
                }
            }

            return hasMissingProperties
        }

        internal fun printPlan(configurationObjects: Collection<ConfigurationObject>, service: ConfService) {
            println("The following changes are going to be applied:")
            configurationObjects.forEach {
                // Those objects will be fetched again later... A better modelization of the plan & operations
                // could prevent us from fetching the objects multiple times.
                val operationType = if (service.retrieveObject(it.reference) == null)
                    ImportOperationType.CREATE
                else ImportOperationType.UPDATE
                printObjectImportStatus(it.reference, operationType)
            }
            println()
        }

        internal fun importConfigurationObject(
            configurationObject: ConfigurationObject,
            service: IConfService
        ): Boolean {
            val reference = configurationObject.reference
            val cfgObject = configurationObject.updateCfgObject(service)
            val type = cfgObject.objectType.toShortName()

            Logging.info { "Processing $type '$reference'." }
            val status = if (cfgObject.isSaved) ImportOperationType.UPDATE else ImportOperationType.CREATE
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
    "Cannot import configuration: some configuration objects' dependencies could not be found."
)

class MandatoryPropertiesNotSetException : Exception(
    "Cannot import configuration: some configuration objects' mandatory properties for creation are not set."
)

class ConfigurationObjectCycleException : Exception(
    "Cannot import configuration: dependency cycles is not yet supported."
)

enum class ImportOperationType(val symbol: String) {
    CREATE("+"), UPDATE("~"), SKIP("=");

    override fun toString() = symbol
}
