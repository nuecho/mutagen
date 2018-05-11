package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.core.JsonGenerationException
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class CategorizedPropertiesSerializer @JvmOverloads constructor(type: Class<CategorizedProperties>? = null) :
    StdSerializer<CategorizedProperties>(type) {

    override fun serialize(
        categorizedProperties: CategorizedProperties,
        generator: JsonGenerator,
        provider: SerializerProvider
    ) = serializeMap(categorizedProperties, generator, provider)

    @Suppress("UNCHECKED_CAST")
    private fun serializeMap(map: Map<String, Any>, generator: JsonGenerator, provider: SerializerProvider) {
        with(generator) {
            writeStartObject()

            for ((key, value) in map) {
                writeFieldName(key)

                when (value) {
                    is Int -> writeNumber(value)
                    is String -> writeString(value)
                    is ByteArray -> writeByteArray(value)
                    is Map<*, *> -> serializeMap(value as Map<String, Any>, generator, provider)
                    else -> throw JsonGenerationException("Unexpected value of type ${value::class})", generator)
                }
            }

            writeEndObject()
        }
    }
}

private fun JsonGenerator.writeByteArray(byteArray: ByteArray) {
    writeStartArray()
    writeBinary(byteArray)
    writeEndArray()
}
