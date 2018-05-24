package com.nuecho.genesys.cli.models.configuration.reference

import com.fasterxml.jackson.annotation.JsonIgnore
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
    val number: String,
    val switch: SwitchReference,
    val type: String,
    val name: String?,
    @JsonIgnore var tenant: TenantReference?
) : ConfigurationObjectReference<CfgDN>(CfgDN::class.java) {

    constructor(
        number: String,
        switch: String,
        type: CfgDNType,
        name: String? = null,
        tenant: TenantReference? = null
    ) : this(number, SwitchReference(switch, tenant), type.toShortName(), name, tenant)

    constructor(dn: CfgDN) : this(
        number = dn.number,
        switch = dn.switch.getReference(),
        type = dn.type.toShortName(),
        name = dn.name,
        tenant = dn.tenant.getReference()
    )

    override fun toQuery(service: IConfService): CfgDNQuery {
        val query = CfgDNQuery()
        query.tenantDbid = service.getObjectDbid(tenant) ?: throw ConfigurationObjectNotFoundException(tenant)
        query.dnNumber = number
        query.switchDbid = service.getObjectDbid(switch) ?: throw ConfigurationObjectNotFoundException(switch)
        query.dnType = ConfigurationObjects.toCfgDNType(type)
        if (name != null) query.name = name
        return query
    }

    override fun compareTo(other: ConfigurationObjectReference<*>): Int {
        if (other !is DNReference) return super.compareTo(other)

        return Comparator
            .comparing { reference: DNReference -> reference.tenant ?: TenantReference("") }
            .thenComparing(DNReference::switch)
            .thenComparing(DNReference::number)
            .thenComparing(DNReference::type)
            .thenComparing { dn -> dn.name ?: "" }
            .compare(this, other)
    }

    override fun toString() = "number: '$number'" +
            ", switch: '${switch.primaryKey}'" +
            ", type: '$type'" +
            (if (name != null) ", name: '$name'" else "") +
            ", tenant: '${tenant?.primaryKey}'"

    @Suppress("DataClassContainsFunctions")
    fun updateTenantReferences(tenantReference: TenantReference) {
        tenant = tenantReference
        switch.tenant = tenantReference
    }
}
