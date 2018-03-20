package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.core.JsonGenerationException
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class KeyValueCollectionSerializer @JvmOverloads constructor(type: Class<Map<String, Any>>? = null) :
    StdSerializer<Map<String, Any>>(type) {

    @Suppress("UNCHECKED_CAST")
    override fun serialize(map: Map<String, Any>, generator: JsonGenerator, provider: SerializerProvider) {
        with(generator) {
            writeStartObject()

            for ((key, value) in map) {
                writeFieldName(key)

                when (value) {
                    is Int -> writeNumber(value)
                    is String -> writeString(value)
                    is ByteArray -> writeByteArray(value)
                    is Map<*, *> -> serialize(value as Map<String, Any>, generator, provider)
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
