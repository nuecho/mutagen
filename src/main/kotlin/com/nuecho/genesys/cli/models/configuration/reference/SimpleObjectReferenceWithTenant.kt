package com.nuecho.genesys.cli.models.configuration.reference

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.ContextualKeyDeserializer
import com.genesyslab.platform.applicationblocks.com.ICfgObject

abstract class SimpleObjectReferenceWithTenant<T : ICfgObject>(
    cfgObjectClass: Class<T>,
    primaryKey: String,
    @Transient var tenant: TenantReference? = null
) : SimpleObjectReference<T>(cfgObjectClass, primaryKey) {
    override fun toString() = "${tenant?.primaryKey}/$primaryKey"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimpleObjectReferenceWithTenant<*>

        if (primaryKey != other.primaryKey) return false
        if (tenant != other.tenant) return false

        return true
    }

    @Suppress("MagicNumber")
    override fun hashCode() = primaryKey.hashCode() + 31 * (tenant?.primaryKey?.hashCode() ?: 0)
}

class SimpleObjectReferenceWithTenantDeserializer(
    private val referenceClass: Class<*>?
) : ContextualDeserializer, JsonDeserializer<SimpleObjectReferenceWithTenant<*>>() {
    constructor() : this(referenceClass = null)

    override fun deserialize(parser: JsonParser, context: DeserializationContext): SimpleObjectReferenceWithTenant<*> {
        val constructor = referenceClass!!.getDeclaredConstructor(String::class.java, TenantReference::class.java)
        return constructor.newInstance(parser.valueAsString, null) as SimpleObjectReferenceWithTenant<*>
    }

    override fun createContextual(context: DeserializationContext, property: BeanProperty?) =
        SimpleObjectReferenceWithTenantDeserializer(context.contextualType.rawClass)
}

class SimpleObjectReferenceWithTenantKeyDeserializer(
    private val referenceClass: Class<*>?
) : ContextualKeyDeserializer, KeyDeserializer() {
    constructor() : this(referenceClass = null)

    override fun deserializeKey(key: String, context: DeserializationContext): SimpleObjectReferenceWithTenant<*> {
        val constructor = referenceClass!!.getDeclaredConstructor(String::class.java, TenantReference::class.java)
        return constructor.newInstance(key, null) as SimpleObjectReferenceWithTenant<*>
    }

    override fun createContextual(context: DeserializationContext, property: BeanProperty) =
        SimpleObjectReferenceWithTenantKeyDeserializer(context.contextualType.keyType.rawClass)
}
