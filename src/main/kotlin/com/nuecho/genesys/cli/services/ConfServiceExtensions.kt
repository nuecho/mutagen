package com.nuecho.genesys.cli.services

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectNotFoundException
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference

fun <T : ICfgObject> IConfService.retrieveObject(reference: ConfigurationObjectReference<T>): T? {
    @Suppress("UNCHECKED_CAST")
    if (ConfServiceCache.containsKey(reference)) return ConfServiceCache[reference] as T

    val query = try {
        reference.toQuery(this)
    } catch (_: ConfigurationObjectNotFoundException) {
        return null
    }
    return retrieveObject(reference.cfgObjectClass, query)
}

fun IConfService.getObjectDbid(reference: ConfigurationObjectReference<*>?): Int? {
    if (reference == null) return null
    val dbid = retrieveObject(reference)?.objectDbid ?: 0
    if (dbid != 0) return dbid
    return null
}
