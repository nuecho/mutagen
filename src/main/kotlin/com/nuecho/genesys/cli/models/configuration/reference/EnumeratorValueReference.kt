package com.nuecho.genesys.cli.models.configuration.reference

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumeratorValue
import com.genesyslab.platform.applicationblocks.com.queries.CfgEnumeratorValueQuery
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectNotFoundException
import com.nuecho.genesys.cli.services.getObjectDbid

data class EnumeratorValueReference(
    val name: String,
    val enumerator: EnumeratorReference
) : ConfigurationObjectReference<CfgEnumeratorValue>(CfgEnumeratorValue::class.java) {

    override fun toQuery(service: IConfService) = CfgEnumeratorValueQuery().also {
        it.name = name
        it.enumeratorDbid = service.getObjectDbid(enumerator) ?: throw ConfigurationObjectNotFoundException(enumerator)
    }

    override fun compareTo(other: ConfigurationObjectReference<*>): Int {
        if (other !is EnumeratorValueReference) return super.compareTo(other)

        return Comparator
            .comparing(EnumeratorValueReference::enumerator)
            .thenComparing(EnumeratorValueReference::name)
            .compare(this, other)
    }

    override fun toString() = "name: '$name', enumerator: '$enumerator'"

    @Suppress("DataClassContainsFunctions")
    fun updateTenantReferences(tenantReference: TenantReference) {
        enumerator.tenant = tenantReference
    }
}
