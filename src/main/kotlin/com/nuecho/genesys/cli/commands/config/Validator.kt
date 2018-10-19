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

package com.nuecho.genesys.cli.commands.config

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.nuecho.genesys.cli.Logging
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.retrieveObject
import kotlin.reflect.KClass

const val PRINT_MARGIN: String = "  "
const val OBJECT_REFERENCE_PREFIX: String = "$PRINT_MARGIN- "
const val LIST_ELEMENT_PREFIX: String = "$PRINT_MARGIN$PRINT_MARGIN- "
const val LIST_ELEMENT_SEPARATOR: String = "\n$LIST_ELEMENT_PREFIX"

class Validator(val configuration: Configuration, val service: ConfService) {

    fun validateConfiguration() {

        Logging.info { "Beginning validation." }

        val validationErrors = findValidationErrors()

        if (validationErrors.isNotEmpty()) {
            throw ValidationException(validationErrors)
        }

        Logging.info { "Validation complete." }
    }

    internal fun findMissingProperties() =
        configuration.asList
            .filter { service.retrieveObject(it.reference) == null }
            .mapNotNull {
                val misses = it.checkMandatoryProperties(configuration, service)
                if (misses.isEmpty()) null else MissingProperties(it, misses)
            }

    internal fun findUnchangeableProperties() =
        configuration.asList
            .mapNotNull { configurationObject ->
                val cfgObject = service.retrieveObject(configurationObject.reference) as CfgObject?
                cfgObject?.let { it ->
                    val unchangeableProperties = configurationObject.checkUnchangeableProperties(it)
                    if (unchangeableProperties.isEmpty()) null
                    else UnchangeableProperties(configurationObject, unchangeableProperties)
                }
            }

    internal fun findMissingDependencies(): List<MissingDependencies> {
        val misses: MutableList<MissingDependencies> = mutableListOf()

        for (configurationObject in configuration.asList) {
            val missingReferences = configurationObject.getReferences()
                .filter { !configuration.asMapByReference.containsKey(it) }
                .filter { service.retrieveObject(it) == null }.toSet()

            if (!missingReferences.isEmpty()) {
                misses.add(MissingDependencies(configurationObject, missingReferences))
            }
        }

        return misses
    }

    internal fun findValidationErrors() =
        findMissingProperties()
            .plus(findMissingDependencies())
            .plus(findUnchangeableProperties())
}

class ValidationException(validationErrors: List<ValidationError>) : Exception(
    "Validation failed."
            + validationErrors.toConsoleString(MissingProperties::class)
            + validationErrors.toConsoleString(MissingDependencies::class)
            + validationErrors.toConsoleString(UnchangeableProperties::class)
)

fun List<ValidationError>.toConsoleString(classType: KClass<*>): String {
    val errors = this.filter { classType.isInstance(it) }
    return if (errors.isEmpty()) ""
    else "\n${errors[0].describe()}\n${errors.map { it.toConsoleString() }.joinToString(separator = "\n")}"
}

interface ValidationError {
    fun describe(): String
    fun toConsoleString(): String
}

data class MissingDependencies(
    val configurationObject: ConfigurationObject,
    val dependencies: Set<ConfigurationObjectReference<*>>
) : ValidationError {

    override fun describe() = "Missing dependencies:"
    override fun toConsoleString() = configurationObject.toConsoleString() +
            dependencies
                .map { it.toConsoleString() }
                .toConsoleString()
}

data class MissingProperties(
    val configurationObject: ConfigurationObject,
    val properties: Set<String>
) : ValidationError {

    override fun describe() = "Missing properties:"
    override fun toConsoleString() = "${configurationObject.toConsoleString()}${properties.toConsoleString()}"
}

data class UnchangeableProperties(
    val configurationObject: ConfigurationObject,
    val properties: Set<String>
) : ValidationError {

    override fun describe() = "Unchangeable properties:"
    override fun toConsoleString() = "${configurationObject.toConsoleString()}${properties.toConsoleString()}"
}

fun ConfigurationObject.toConsoleString() = "$OBJECT_REFERENCE_PREFIX${this.reference.toConsoleString()}:\n"

fun Collection<*>.toConsoleString() =
    this.joinToString(prefix = LIST_ELEMENT_PREFIX, separator = LIST_ELEMENT_SEPARATOR)
