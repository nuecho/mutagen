@file:Suppress("MatchingDeclarationName")

package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.commons.collections.KeyValueCollection
import com.genesyslab.platform.commons.collections.KeyValuePair

private fun KeyValueCollection.asMap(): Map<String, Any> =
    this.map {
        val keyValuePair = it as KeyValuePair
        var value = keyValuePair.value!!

        if (value is KeyValueCollection) {
            value = value.asMap()
        }

        keyValuePair.stringKey!! to value
    }.toMap()

@Suppress("UNCHECKED_CAST")
fun KeyValueCollection.asCategorizedProperties(): CategorizedProperties? {
    val categorizedProperties = this.asMap()

    if (categorizedProperties.isEmpty()) return null // so we don't turn out serializing top level empty map
    if (!categorizedProperties.filterValues { it !is Map<*, *> }.isEmpty())
        throw InvalidKeyValueCollectionException(
            "Key Value Collection must contains sections (KeyValueCollection of KeyValueCollection)."
        )

    return categorizedProperties as CategorizedProperties
}

typealias CategorizedProperties = Map<String, Map<String, Any>>

class InvalidKeyValueCollectionException(message: String) : Exception(message)
