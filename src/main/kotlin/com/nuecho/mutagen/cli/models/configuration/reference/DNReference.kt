/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nuecho.mutagen.cli.models.configuration.reference

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.queries.CfgDNQuery
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectNotFoundException
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.toShortName

data class DNReference(
    val number: String,
    val switch: SwitchReference,
    val type: String,
    val name: String? = null,
    @JsonIgnore var tenant: TenantReference? = null
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

    override fun toQuery(service: ConfService): CfgDNQuery {
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
