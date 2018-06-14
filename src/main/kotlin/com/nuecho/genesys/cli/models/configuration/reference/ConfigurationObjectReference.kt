package com.nuecho.genesys.cli.models.configuration.reference

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgQuery
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.nuecho.genesys.cli.core.setBuilder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.getCfgObjectType

abstract class ConfigurationObjectReference<T : ICfgObject>(
    @get:JsonIgnore
    val cfgObjectClass: Class<T>
) : Comparable<ConfigurationObjectReference<*>> {

    abstract fun toQuery(service: IConfService): ICfgQuery

    @JsonIgnore
    fun getCfgObjectType() = getCfgObjectType(cfgObjectClass)

    override fun compareTo(other: ConfigurationObjectReference<*>) =
        getCfgObjectType().name().compareTo(other.getCfgObjectType().name())
}

fun referenceSetBuilder() = setBuilder<ConfigurationObjectReference<*>>()
