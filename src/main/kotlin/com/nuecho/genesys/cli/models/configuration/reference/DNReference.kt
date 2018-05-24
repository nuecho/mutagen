package com.nuecho.genesys.cli.models.configuration.reference

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.ICfgQuery
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.queries.CfgDNQuery
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectNotFoundException
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

data class DNReference(
    @JsonIgnore var tenant: TenantReference?,
    val switch: SwitchReference,
    val number: String,
    val type: String,
    val name: String?
) : ConfigurationObjectReference<CfgDN>(CfgDN::class.java) {

    constructor(
        switch: String,
        number: String,
        type: CfgDNType,
        name: String? = null,
        tenant: TenantReference? = null
    ) : this(tenant, SwitchReference(switch, tenant), number, type.toShortName(), name)

    constructor(dn: CfgDN) : this(
        tenant = dn.tenant.getReference(),
        switch = dn.switch.getReference(),
        number = dn.number,
        type = dn.type.toShortName(),
        name = dn.name
    )

    override fun compareTo(other: ConfigurationObjectReference<*>): Int {
        if (other !is DNReference) return super.compareTo(other)

        return Comparator.comparing(DNReference::switch)
            .thenComparing(DNReference::number)
            .thenComparing(DNReference::type)
            .thenComparing { dn -> dn.name ?: "" }
            .compare(this, other)
    }

    override fun toQuery(service: IConfService): ICfgQuery? {
        val query = CfgDNQuery()
        query.tenantDbid = service.getObjectDbid(tenant) ?: throw ConfigurationObjectNotFoundException(tenant)
        query.dnNumber = number
        query.switchDbid = service.getObjectDbid(switch) ?: throw ConfigurationObjectNotFoundException(switch)
        query.dnType = ConfigurationObjects.toCfgDNType(type)
        if (name != null) query.name = name
        return query
    }

    override fun toString() = "tenant: '${tenant?.primaryKey}'" +
            ", number: '$number'" +
            ", switch: '${switch.primaryKey}'" +
            ", type: '$type'" +
            if (name != null) ", name: '$name'" else ""
}

fun DNReference.updateTenantReferences(tenantReference: TenantReference) {
    tenant = tenantReference
    switch.tenant = tenantReference
}
