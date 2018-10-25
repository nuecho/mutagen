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

package com.nuecho.mutagen.cli.models.configuration.reference

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.genesyslab.platform.applicationblocks.com.ICfgObject

abstract class SimpleObjectReference<T : ICfgObject>(cfgObjectClass: Class<T>, open val primaryKey: String) :
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
