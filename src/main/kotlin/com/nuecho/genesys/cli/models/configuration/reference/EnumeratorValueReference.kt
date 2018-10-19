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
