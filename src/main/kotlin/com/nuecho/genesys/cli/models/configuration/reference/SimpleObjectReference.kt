package com.nuecho.genesys.cli.models.configuration.reference

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.genesyslab.platform.applicationblocks.com.ICfgObject

abstract class SimpleObjectReference<T : ICfgObject>(cfgObjectClass: Class<T>, val primaryKey: String) :
    ConfigurationObjectReference<T>(cfgObjectClass) {
    override fun compareTo(other: ConfigurationObjectReference<*>): Int {
        if (other !is SimpleObjectReference) return super.compareTo(other)

        return Comparator.comparing { r: SimpleObjectReference<*> -> r.getCfgObjectType().name() }
            .thenComparing(SimpleObjectReference<*>::primaryKey)
            .compare(this, other)
    }

    override fun toString() = primaryKey

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimpleObjectReference<*>

        if (primaryKey != other.primaryKey) return false

        return true
    }

    override fun hashCode() = primaryKey.hashCode()
}

class SimpleObjectReferenceSerializer : JsonSerializer<SimpleObjectReference<*>>() {
    override fun serialize(value: SimpleObjectReference<*>, generator: JsonGenerator, serializers: SerializerProvider) =
        generator.writeString(value.primaryKey)
}

class SimpleObjectReferenceDeserializer(
    private val referenceClass: Class<*>?
) : ContextualDeserializer, JsonDeserializer<SimpleObjectReference<*>>() {
    constructor() : this(referenceClass = null)

    override fun deserialize(parser: JsonParser, context: DeserializationContext): SimpleObjectReference<*> {
        val constructor = referenceClass!!.getDeclaredConstructor(String::class.java)
        return constructor.newInstance(parser.valueAsString) as SimpleObjectReference<*>
    }

    override fun createContextual(context: DeserializationContext, property: BeanProperty?) =
        SimpleObjectReferenceDeserializer(context.contextualType.rawClass)
}

class SimpleObjectReferenceKeySerializer : JsonSerializer<SimpleObjectReference<*>>() {
    override fun serialize(value: SimpleObjectReference<*>, generator: JsonGenerator, serializers: SerializerProvider) =
        generator.writeFieldName(value.primaryKey)
}
