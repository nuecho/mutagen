package com.nuecho.genesys.cli.services

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import kotlin.reflect.KProperty

class TenantCacheDelegate {
    var cache: Collection<CfgTenant>? = null

    operator fun getValue(thisRef: IConfService, property: KProperty<*>): Collection<CfgTenant> {
        cache = cache ?: thisRef.retrieveTenants()
        return cache!!
    }
}
