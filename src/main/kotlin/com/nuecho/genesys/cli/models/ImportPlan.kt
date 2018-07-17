package com.nuecho.genesys.cli.models

import com.nuecho.genesys.cli.models.ImportOperationType.CREATE
import com.nuecho.genesys.cli.models.ImportOperationType.SKIP
import com.nuecho.genesys.cli.models.ImportOperationType.UPDATE
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

const val PRINT_MARGIN: String = "  "
val typeToColor: Map<ImportOperationType, String> = mapOf(
    CREATE to "green",
    UPDATE to "yellow",
    SKIP to "white"
)

class ImportPlan(val configuration: Configuration, val service: ConfService) {

    private val operations = toOperations(service, configuration)
    private val missingProperties = checkMissingProperties(service, operations)
    private val missingDependencies = checkMissingDependencies(service, configuration)
    private var orderedOperations = applyOperationOrder(configuration, operations)

    fun check() = Validations(missingDependencies, missingProperties)

    fun print() = printPlan(orderedOperations)

    fun apply() =
        orderedOperations
            .map { it.apply() }
            .filter { it }
            .count()

    companion object {

        internal fun toOperations(service: ConfService, configuration: Configuration): List<ImportPlanOperation> =
            configuration.asMapByReference.values.map { ImportPlanOperation(service, it) }

        private fun checkMissingProperties(
            service: ConfService,
            operations: List<ImportPlanOperation>
        ): List<MissingProperties> {

            val misses = operations
                .filter { it.type == CREATE }
                .mapNotNull {
                    val misses = it.configurationObject.checkMandatoryProperties(service)
                    if (misses.isEmpty()) null else MissingProperties(it.configurationObject, misses)
                }

            // XXX this should be dealt with externally to the ImportPlan itself
            if (!misses.isEmpty()) throw MandatoryPropertiesNotSetException(misses)

            return misses
        }

        private fun checkMissingDependencies(
            service: ConfService,
            configuration: Configuration
        ): List<MissingDependencies> {

            val misses: MutableList<MissingDependencies> = mutableListOf<MissingDependencies>()

            for (configurationObject in configuration.asMapByReference.values) {
                val missingReferences = configurationObject.getReferences()
                    .filter { !configuration.asMapByReference.containsKey(it) }
                    .filter { service.retrieveObject(it) == null }.toSet()

                if (!missingReferences.isEmpty()) {
                    misses.add(MissingDependencies(configurationObject, missingReferences))
                }
            }

            // XXX this should be dealt with externally to the ImportPlan itself
            if (!misses.isEmpty()) throw UnresolvedConfigurationObjectReferenceException(misses)

            return misses
        }

        internal fun applyOperationOrder(
            configuration: Configuration,
            operations: List<ImportPlanOperation>
        ): List<ImportPlanOperation> {

            val dependencyGraph: Graph<ImportPlanOperation, DefaultEdge> = DefaultDirectedGraph(DefaultEdge::class.java)

            // We need to add graph vertex before we connect them together
            operations.forEach { operation -> dependencyGraph.addVertex(operation) }

            // We care only for dependencies part of the configuration we are trying to import
            // assuming at this state that remote dependencies exists and checked for
            for (operation in operations) {
                operation.configurationObject.getReferences()
                    .filter { reference -> configuration.asMapByReference.containsKey(reference) }
                    .forEach { reference ->
                        val from = operations.find { it.configurationObject.reference == reference }

                        dependencyGraph.addEdge(from, operation)
                    }
            }

            if (CycleDetector(dependencyGraph).detectCycles()) throw ConfigurationObjectCycleException()

            return TopologicalOrderIterator(dependencyGraph).asSequence().toList()
        }

        internal fun printPlan(operations: List<ImportPlanOperation>) {
            println("The following changes are going to be applied:")
            operations.forEach { it.print() }
            println()
        }
    }
}

class UnresolvedConfigurationObjectReferenceException(misses: List<MissingDependencies>) : Exception(
    "Cannot import configuration: some configuration objects' dependencies could not be found.\n\t" +
            misses.map { it.toString() }.joinToString("\n$PRINT_MARGIN")
)

class MandatoryPropertiesNotSetException(misses: List<MissingProperties>) : Exception(
    "Cannot import configuration: some configuration objects' mandatory properties for creation are not set.\n\t" +
            misses.map { it.toString() }.joinToString("\n$PRINT_MARGIN")
)

class ConfigurationObjectCycleException : Exception(
    "Cannot import configuration: dependency cycles is not yet supported."
)

data class Validations(
    val missingDependencies: List<MissingDependencies>,
    val missingProperties: List<MissingProperties>
)

data class MissingProperties(
    val configurationObject: ConfigurationObject,
    val properties: Set<String>
) {

    // XXX formatting should be dealt with externally
    override fun toString(): String =
        "Mandatory properties [${properties.joinToString()}] " +
                "not set in ${configurationObject.reference.getCfgObjectType().toShortName()} " +
                "'${configurationObject.reference}'"
}

data class MissingDependencies(
    val configurationObject: ConfigurationObject,
    val dependencies: Set<ConfigurationObjectReference<*>>
) {

    // XXX formatting should be dealt with externally
    override fun toString(): String =
        dependencies.map {
            "Cannot find ${it.getCfgObjectType().toShortName()} '$it' " +
                    "(referenced by ${configurationObject.reference.getCfgObjectType().toShortName()} " +
                    "'${configurationObject.reference}')"
        }.joinToString()
}

enum class ImportOperationType(val symbol: String) {
    CREATE("+"), UPDATE("~"), SKIP("=");

    override fun toString() = symbol
}
