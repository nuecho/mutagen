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
