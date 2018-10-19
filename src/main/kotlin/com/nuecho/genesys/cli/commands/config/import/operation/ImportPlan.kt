/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nuecho.genesys.cli.commands.config.import.operation

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.nuecho.genesys.cli.Logging
import com.nuecho.genesys.cli.commands.config.import.operation.ImportOperationType.CREATE
import com.nuecho.genesys.cli.commands.config.import.operation.ImportOperationType.SKIP
import com.nuecho.genesys.cli.commands.config.import.operation.ImportOperationType.UPDATE
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.models.configuration.ConfigurationBuilder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.retrieveObject
import org.jgrapht.Graph
import org.jgrapht.alg.cycle.CycleDetector
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.traverse.TopologicalOrderIterator

val typeToColor: Map<ImportOperationType, String> = mapOf(
    CREATE to "green",
    UPDATE to "yellow",
    SKIP to "white"
)

class ImportPlan(val configuration: Configuration, val service: ConfService) {
    private val orderedOperations = applyOperationOrder(service, configuration)

    fun print() = printPlan(orderedOperations)

    fun apply(): Map<ImportOperationType, Int> {
        for (operation in orderedOperations) {
            operation.configurationObject.reference.let {
                Logging.info { "Processing ${it.toConsoleString()}." }
            }

            operation.apply()
            operation.print(false)
        }

        val count = mutableMapOf(CREATE to 0, SKIP to 0, UPDATE to 0)

        return orderedOperations.fold(count) { accumulator, importOperation ->
            accumulator.apply {
                when (importOperation.type) {
                    CREATE -> accumulator[CREATE] = accumulator[CREATE]!! + 1
                    SKIP -> accumulator[SKIP] = accumulator[SKIP]!! + 1
                    UPDATE -> accumulator[UPDATE] = accumulator[UPDATE]!! + 1
                }
            }
        }
    }

    companion object {

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

                // Safety net: check for cycles again...
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

        private fun toOperations(service: ConfService, configuration: Configuration) =
            configuration.asMapByReference.values.map { toOperation(service, it) }

        internal fun toOperation(service: ConfService, configurationObject: ConfigurationObject): ImportOperation {
            val remoteCfgObject = service.retrieveObject(configurationObject.reference)

            return when {
                remoteCfgObject == null -> CreateOperation(configurationObject, service)
                isIdenticalAfterUpdate(service, configurationObject, remoteCfgObject) ->
                    SkipOperation(configurationObject, service)
                else -> UpdateOperation(configurationObject, remoteCfgObject, service)
            }
        }

        private fun isIdenticalAfterUpdate(
            service: ConfService,
            configurationObject: ConfigurationObject,
            remoteCfgObject: ICfgObject
        ): Boolean {
            val remoteObject = ConfigurationBuilder.toConfigurationObject(remoteCfgObject)!!
            // Makes a copy of the remoteCfgObject because updateCfgObject mutates it (which sucks BTW)
            val remoteCfgObjectCopy = remoteObject.createCfgObject(service)

            val updatedCfgObject = configurationObject.updateCfgObject(service, remoteCfgObjectCopy)
            val updatedObject = ConfigurationBuilder.toConfigurationObject(updatedCfgObject)!!

            return remoteObject.toJson() == updatedObject.toJson()
        }

        private fun createOperationGraph(operations: List<ImportOperation>): Graph<ImportOperation, DefaultEdge> {
            val graph: Graph<ImportOperation, DefaultEdge> = DefaultDirectedGraph(DefaultEdge::class.java)

            // We need to add graph vertex before we connect them together
            operations.forEach { operation -> graph.addVertex(operation) }

            // We care only for dependencies part of the configuration we are trying to import
            // assuming at this state that remote dependencies exist.
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

enum class ImportOperationType(val symbol: String) {
    CREATE("+"), UPDATE("~"), SKIP("=");

    override fun toString() = symbol
}
