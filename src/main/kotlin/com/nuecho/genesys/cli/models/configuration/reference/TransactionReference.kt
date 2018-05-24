package com.nuecho.genesys.cli.models.configuration.reference

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction
import com.genesyslab.platform.applicationblocks.com.queries.CfgTransactionQuery
import com.genesyslab.platform.configuration.protocol.types.CfgTransactionType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectNotFoundException
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgTransactionType
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

data class TransactionReference(
    val name: String,
    val type: String,
    @JsonIgnore var tenant: TenantReference?
) : ConfigurationObjectReference<CfgTransaction>(CfgTransaction::class.java) {

    constructor(
        name: String,
        type: CfgTransactionType,
        tenant: TenantReference? = null
    ) : this(name, type.toShortName(), tenant)

    override fun toQuery(service: IConfService) = CfgTransactionQuery(name).also {
        it.objectType = toCfgTransactionType(type)
        it.tenantDbid = service.getObjectDbid(tenant) ?: throw ConfigurationObjectNotFoundException(tenant)
    }

    override fun compareTo(other: ConfigurationObjectReference<*>): Int {
        if (other !is TransactionReference) return super.compareTo(other)

        return Comparator
            .comparing { reference: TransactionReference -> reference.tenant ?: TenantReference("") }
            .thenComparing(TransactionReference::name)
            .thenComparing(TransactionReference::type)
            .compare(this, other)
    }

    override fun toString() = "name: '$name', type: '$type', tenant: '${tenant?.primaryKey}'"
}
