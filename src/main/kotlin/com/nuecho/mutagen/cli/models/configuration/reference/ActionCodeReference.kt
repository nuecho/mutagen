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
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.applicationblocks.com.queries.CfgActionCodeQuery
import com.genesyslab.platform.configuration.protocol.types.CfgActionCodeType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectNotFoundException
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgActionCodeType
import com.nuecho.mutagen.cli.services.getObjectDbid
import com.nuecho.mutagen.cli.toShortName

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
