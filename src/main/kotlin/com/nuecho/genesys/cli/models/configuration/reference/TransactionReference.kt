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
