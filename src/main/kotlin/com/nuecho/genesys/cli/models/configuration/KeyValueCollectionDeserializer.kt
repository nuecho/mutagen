package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

class KeyValueCollectionDeserializer @JvmOverloads constructor(type: Class<*>? = null) :
    StdDeserializer<Map<String, Any>>(type) {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): Map<String, Any> =
        parser.codec.readTree<JsonNode>(parser).let {
            return if (it is ObjectNode) deserializeMap(it, parser)
            else throw JsonParseException(parser, "Unexpected node value ($it)")
        }

    private fun deserializeMap(node: ObjectNode, parser: JsonParser): Map<String, Any> =
        node.fields()
            .asSequence()
            .associateBy({ it.key }, { deserializeValue(it.value, parser) })

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
