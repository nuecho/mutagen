package com.nuecho.genesys.cli.commands.config.import.operation

import com.nuecho.genesys.cli.Logging
import com.nuecho.genesys.cli.commands.config.import.operation.ImportOperationType.CREATE
import com.nuecho.genesys.cli.commands.config.import.operation.ImportOperationType.SKIP
import com.nuecho.genesys.cli.commands.config.import.operation.ImportOperationType.UPDATE
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
    private val orderedOperations = applyOperationOrder(service, configuration)
    val missingProperties = findMissingProperties(service, orderedOperations)
    val missingDependencies = findMissingDependencies(service, configuration)

    fun print() = printPlan(orderedOperations)

    fun apply(): Int {
        for (operation in orderedOperations) {
            operation.configurationObject.reference.let {
                Logging.info { "Processing ${it.getCfgObjectType().toShortName()} '$it'." }
            }

            operation.apply()
            operation.print(false)
        }

        // FIXME: We should probably expose the count for each operation types instead (see AR-359)
        return orderedOperations.filter { it !is UpdateReferenceOperation }.count()
    }

    companion object {

        internal fun findMissingProperties(service: ConfService, operations: List<ImportOperation>) = operations
            .filter { it.type == CREATE }
            .mapNotNull {
                val misses = it.configurationObject.checkMandatoryProperties(service)
                if (misses.isEmpty()) null else MissingProperties(it.configurationObject, misses)
            }

        internal fun findMissingDependencies(service: ConfService, configuration: Configuration):
                List<MissingDependencies> {
            val misses: MutableList<MissingDependencies> = mutableListOf()

            for (configurationObject in configuration.asMapByReference.values) {
                val missingReferences = configurationObject.getReferences()
                    .filter { !configuration.asMapByReference.containsKey(it) }
                    .filter { service.retrieveObject(it) == null }.toSet()

                if (!missingReferences.isEmpty()) {
                    misses.add(MissingDependencies(configurationObject, missingReferences))
                }
            }

            return misses
        }

        internal fun applyOperationOrder(service: ConfService, configuration: Configuration):
                List<ImportOperation> {
            val operations = toOperations(service, configuration).toMutableList()
            var operationGraph = createOperationGraph(operations)

            if (operationGraph.detectCycles()) {
                val cyclingNodes = operationGraph.findCycles().filter { it.configurationObject.cloneBare() != null }

                operations -= cyclingNodes
                cyclingNodes
                    .map { it.configurationObject }
                    .forEach { configurationObject ->
                        operations += CreateOperation(configurationObject.cloneBare()!!, service)
                        operations += UpdateReferenceOperation(configurationObject, service)
                    }

                operationGraph = createOperationGraph(operations)

                // Safety net: check for cycle again...
                if (operationGraph.detectCycles())
                    throw IllegalArgumentException("Could not break dependency cycle(s).")
            }

            return TopologicalOrderIterator(operationGraph).asSequence().toList()
        }

        internal fun printPlan(operations: List<ImportOperation>) {
            println("The following changes are going to be applied:")
            operations.forEach { it.print(true) }
            println()
        }

        internal fun toOperations(service: ConfService, configuration: Configuration) =
            configuration.asMapByReference.values.map { toOperation(service, it) }

        internal fun toOperation(service: ConfService, configurationObject: ConfigurationObject):
                ImportOperation {
            val remoteCfgObject = service.retrieveObject(configurationObject.reference)

            return if (remoteCfgObject == null) CreateOperation(configurationObject, service)
            else UpdateOperation(configurationObject, remoteCfgObject, service)
        }

        private fun createOperationGraph(operations: List<ImportOperation>):
                Graph<ImportOperation, DefaultEdge> {
            val graph: Graph<ImportOperation, DefaultEdge> = DefaultDirectedGraph(DefaultEdge::class.java)

            // We need to add graph vertex before we connect them together
            operations.forEach { operation -> graph.addVertex(operation) }

            // We care only for dependencies part of the configuration we are trying to import
            // assuming at this state that remote dependencies exists.
            for (operation in operations) {
                for (reference in operation.configurationObject.getReferences()) {
                    // Cycles are problematic only for objects we are CREATING - thus the filter on type == CREATE.
                    val dependee = operations
                        .filter { it.type == CREATE }
                        .firstOrNull { it.configurationObject.reference == reference }

                    if (dependee != null) {
                        graph.addEdge(dependee, operation)
                    }
                }
            }

            return graph
        }

        private fun Graph<*, *>.detectCycles() = CycleDetector(this).detectCycles()

        private fun <V, E> Graph<V, E>.findCycles() = CycleDetector(this).findCycles()
    }
}

data class MissingProperties(
    val configurationObject: ConfigurationObject,
    val properties: Set<String>
)

data class MissingDependencies(
    val configurationObject: ConfigurationObject,
    val dependencies: Set<ConfigurationObjectReference<*>>
)

enum class ImportOperationType(val symbol: String) {
    CREATE("+"), UPDATE("~"), SKIP("=");

    override fun toString() = symbol
}
