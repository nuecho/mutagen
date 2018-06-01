package com.nuecho.genesys.cli.services

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.nuecho.genesys.cli.Logging
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.toShortName

fun <T : ICfgObject> IConfService.retrieveObject(reference: ConfigurationObjectReference<T>): T? =
    retrieveObject(reference.cfgObjectClass, reference.toQuery(this))

fun IConfService.getObjectDbid(reference: ConfigurationObjectReference<*>?): Int? {
    if (reference == null) return null
    val dbid = retrieveObject(reference)?.objectDbid ?: 0
    if (dbid != 0) return dbid
    Logging.warn { "Cannot find ${reference.getCfgObjectType().toShortName()} '$reference'" }
    return null
}
