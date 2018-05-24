package com.nuecho.genesys.cli.models.configuration.reference

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.applicationblocks.com.queries.CfgActionCodeQuery
import com.genesyslab.platform.configuration.protocol.types.CfgActionCodeType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectNotFoundException
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgActionCodeType
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

data class ActionCodeReference(
    val name: String,
    val type: String,
    @JsonIgnore var tenant: TenantReference?
) : ConfigurationObjectReference<CfgActionCode>(CfgActionCode::class.java) {

    constructor(
        name: String,
        type: CfgActionCodeType,
        tenant: TenantReference? = null
    ) : this(name, type.toShortName(), tenant)

    override fun toQuery(service: IConfService) = CfgActionCodeQuery(name).also {
        it.tenantDbid = service.getObjectDbid(tenant) ?: throw ConfigurationObjectNotFoundException(tenant)
        it.name = name
        it.codeType = toCfgActionCodeType(type)
    }

    override fun compareTo(other: ConfigurationObjectReference<*>): Int {
        if (other !is ActionCodeReference) return super.compareTo(other)

        return Comparator
            .comparing { reference: ActionCodeReference -> reference.tenant ?: TenantReference("") }
            .thenComparing(ActionCodeReference::name)
            .thenComparing(ActionCodeReference::type)
            .compare(this, other)
    }

    override fun toString() = "name: '$name', type: '$type', tenant: '${tenant?.primaryKey}'"
}
