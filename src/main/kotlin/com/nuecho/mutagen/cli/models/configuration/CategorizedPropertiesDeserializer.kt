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

package com.nuecho.mutagen.cli.models.configuration

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

class CategorizedPropertiesDeserializer @JvmOverloads constructor(type: Class<*>? = null) :
    StdDeserializer<CategorizedProperties>(type) {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): CategorizedProperties =
        parser.codec.readTree<JsonNode>(parser).let {
            return if (it is ObjectNode) deserializeMapWithSections(it, parser)
            else throw JsonParseException(parser, "Unexpected node value ($it)")
        }

    @Suppress("UNCHECKED_CAST")
    private fun deserializeMapWithSections(node: ObjectNode, parser: JsonParser): CategorizedProperties =
        deserializeMap(node, parser, { value, valueParser ->
            if (value is ObjectNode) deserializeMap(value, valueParser)
            else throw InvalidKeyValueCollectionException(
                "Key Value Collection must contains sections (KeyValueCollection of KeyValueCollection)."
            )
        }) as CategorizedProperties

    private fun deserializeMap(
        node: ObjectNode, parser: JsonParser, deserializer: (JsonNode, JsonParser) -> Any = ::deserializeValue
    ): Map<String, Any> =
        node.fields()
            .asSequence()
            .associateBy({ it.key }, { deserializer(it.value, parser) })

    private fun deserializeValue(node: JsonNode, parser: JsonParser): Any =
        when (node) {
            is NumericNode -> node.asInt()
            is TextNode -> node.asText()
            is ArrayNode -> deserializeBinary(node, parser)
            is ObjectNode -> deserializeMap(node, parser)
            else -> throw JsonParseException(parser, "Unexpected map value ($node)")
        }

    private fun deserializeBinary(node: ArrayNode, parser: JsonParser) =
        if (node.size() == 1 || node[0].isTextual) node[0].binaryValue()
        else throw JsonParseException(parser, "Unexpected binary value ($node)")
}
