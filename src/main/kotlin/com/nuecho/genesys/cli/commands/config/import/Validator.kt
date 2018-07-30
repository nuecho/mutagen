package com.nuecho.genesys.cli.commands.config.import

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

    fun validateConfiguration(): Boolean {

        Logging.info { "Beginning validation." }

        val validationErrors = findValidationErrors()

        if (validationErrors.isNotEmpty()) {
            throw ValidationException(validationErrors)
        }

        Logging.info { "Validation complete." }
        return true
    }

    internal fun findMissingProperties() =
        configuration.asList
            .filter { service.retrieveObject(it.reference) == null }
            .mapNotNull {
                val misses = it.checkMandatoryProperties(configuration, service)
                if (misses.isEmpty()) null else MissingProperties(it, misses)
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
}

class ValidationException(validationErrors: List<ValidationError>) : Exception(
    "Validation failed."
            + validationErrors.toConsoleString(MissingProperties::class)
            + validationErrors.toConsoleString(MissingDependencies::class)
)

fun List<ValidationError>.toConsoleString(classType: KClass<*>): String {
    val errors = this.filter { classType.isInstance(it) }
    return if (errors.isEmpty()) ""
    else "\n${errors[0].describe()}\n${errors.map { it.toConsoleString() }.joinToString(separator = "\n")}"
}

data class MissingDependencies(
    val configurationObject: ConfigurationObject,
    val dependencies: Set<ConfigurationObjectReference<*>>
) : ValidationError {

    override fun describe() = "Missing dependencies:"

    override fun toConsoleString() = "$OBJECT_REFERENCE_PREFIX${configurationObject.reference.toConsoleString()}:\n" +
            dependencies
                .map { it.toConsoleString() }
                .joinToString(prefix = LIST_ELEMENT_PREFIX, separator = LIST_ELEMENT_SEPARATOR)
}

data class MissingProperties(
    val configurationObject: ConfigurationObject,
    val properties: Set<String>
) : ValidationError {

    override fun describe() = "Missing properties:"

    override fun toConsoleString() = "$OBJECT_REFERENCE_PREFIX${configurationObject.reference.toConsoleString()}:\n" +
            properties.joinToString(prefix = LIST_ELEMENT_PREFIX, separator = LIST_ELEMENT_SEPARATOR)
}

interface ValidationError {
    fun describe(): String
    fun toConsoleString(): String
}
