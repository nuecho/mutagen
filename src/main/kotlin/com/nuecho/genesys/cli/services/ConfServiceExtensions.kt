package com.nuecho.genesys.cli.services

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.applicationblocks.com.queries.CfgTenantQuery
import com.nuecho.genesys.cli.Logging
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.toShortName

fun <T : ICfgObject> IConfService.retrieveObject(reference: ConfigurationObjectReference<T>?): T? {
    if (reference == null) return null
    val query = reference.toQuery(this) ?: return null
    return retrieveObject(reference.cfgObjectClass, query)
}

fun IConfService.getObjectDbid(reference: ConfigurationObjectReference<*>?): Int? {
    if (reference == null) return null
    val dbid = retrieveObject(reference)?.objectDbid ?: 0
    if (dbid != 0) return dbid
    Logging.warn { "Cannot find ${reference.getCfgObjectType().toShortName()} '$reference'" }
    return null
}

fun IConfService.retrieveTenants(): Collection<CfgTenant> =
    retrieveMultipleObjects(CfgTenant::class.java, CfgTenantQuery().apply { allTenants = 1 })

val IConfService.tenants: Collection<CfgTenant> by TenantCacheDelegate()

val IConfService.defaultTenantDbid: Int
    get() {
        if (tenants.isEmpty()) throw IllegalStateException("No tenant found.")
        if (tenants.size > 1) throw IllegalStateException("Unsupported multi-tenancy. " + tenants.map { it.name })
        return tenants.first().dbid
    }
