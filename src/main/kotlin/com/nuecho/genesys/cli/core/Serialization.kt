package com.nuecho.genesys.cli.core

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.OutputStream

private val defaultJsonObjectMapper = jsonObjectMapperBase()

private val compactJsonObjectMapper = jsonObjectMapperBase()
    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY) // both empty arrays and object won't be serialized
    .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT) // do not serialize false, 0,

val yamlObjectMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())

fun defaultJsonObjectMapper(): ObjectMapper = defaultJsonObjectMapper
fun compactJsonObjectMapper(): ObjectMapper = compactJsonObjectMapper
fun defaultYamlObjectMapper(): ObjectMapper = yamlObjectMapper

fun defaultJsonGenerator(outputStream: OutputStream = System.out): JsonGenerator =
    jsonGeneratorBase(outputStream).setCodec(defaultJsonObjectMapper)

fun compactJsonGenerator(outputStream: OutputStream = System.out): JsonGenerator =
    jsonGeneratorBase(outputStream).setCodec(compactJsonObjectMapper)

private fun jsonObjectMapperBase() = jacksonObjectMapper()
    .setSerializationInclusion(JsonInclude.Include.NON_NULL) // do not serialize null
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .setDateFormat(StdDateFormat())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true) // consistent output
    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true) // consistent output
    .registerModule(SimpleModule().setDeserializerModifier(InitializingBeanDeserializerModifier())) // InitializingBean

fun jsonGeneratorBase(outputStream: OutputStream): JsonGenerator = JsonFactory()
    .createGenerator(outputStream, JsonEncoding.UTF8)
    .setPrettyPrinter(DefaultPrettyPrinter())

private class InitializingBeanDeserializerModifier : BeanDeserializerModifier() {
    override fun modifyDeserializer(
        config: DeserializationConfig?,
        beanDescription: BeanDescription?,
        deserializer: JsonDeserializer<*>?
    ): JsonDeserializer<*> = InitializingBeanDeserializer(deserializer!!)
}

private class InitializingBeanDeserializer(
    private val deserializer: JsonDeserializer<*>
) : DelegatingDeserializer(deserializer) {
    override fun newDelegatingInstance(newDelegatee: JsonDeserializer<*>): JsonDeserializer<*> = deserializer

    override fun deserialize(parser: JsonParser, context: DeserializationContext): Any {
        val result = _delegatee.deserialize(parser, context)

        if (result is InitializingBean) {
            result.afterPropertiesSet()
        }

        return result
    }
}
