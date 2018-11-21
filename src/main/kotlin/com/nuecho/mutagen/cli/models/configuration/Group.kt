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

package com.nuecho.mutagen.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgGroup
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.mutagen.cli.models.configuration.reference.DNReference
import com.nuecho.mutagen.cli.models.configuration.reference.FolderReference
import com.nuecho.mutagen.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.mutagen.cli.models.configuration.reference.PersonReference
import com.nuecho.mutagen.cli.models.configuration.reference.ScriptReference
import com.nuecho.mutagen.cli.models.configuration.reference.StatTableReference
import com.nuecho.mutagen.cli.models.configuration.reference.TenantReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.getObjectDbid
import com.nuecho.mutagen.cli.toShortName

@Suppress("DataClassContainsFunctions")
data class Group(
    val tenant: TenantReference,
    val name: String,
    val managers: List<PersonReference>? = null,
    val routeDNs: List<DNReference>? = null,
    val capacityTable: StatTableReference? = null,
    val quotaTable: StatTableReference? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    val userProperties: CategorizedProperties? = null,
    val capacityRule: ScriptReference? = null,
    val site: FolderReference? = null,
    val contract: ObjectiveTableReference? = null
) {
    constructor(group: CfgGroup) : this(
        tenant = group.tenant.getReference(),
        name = group.name,
        managers = group.managers?.map { it.getReference() },
        routeDNs = group.routeDNs?.map { it.getReference() },
        capacityTable = group.capacityTable?.getReference(),
        quotaTable = group.quotaTable?.getReference(),
        state = group.state?.toShortName(),
        userProperties = group.userProperties?.asCategorizedProperties(),
        capacityRule = group.capacityRule?.getReference(),
        site = group.site?.getReference(),
        contract = group.contract?.getReference()
    )

    fun toUpdatedCfgGroup(service: IConfService, groupInfo: CfgGroup) = groupInfo.also {
        setProperty("tenantDBID", service.getObjectDbid(tenant), it)
        setProperty("name", name, it)
        setProperty("managerDBIDs", managers?.map { service.getObjectDbid(it) }, it)

        setProperty("routeDNDBIDs", routeDNs?.mapNotNull { service.getObjectDbid(it) }, it)
        setProperty("capacityTableDBID", service.getObjectDbid(capacityTable), it)
        setProperty("quotaTableDBID", service.getObjectDbid(quotaTable), it)
        setProperty("state", toCfgObjectState(state), it)
        setProperty("userProperties", toKeyValueCollection(userProperties), it)
        setProperty("capacityRuleDBID", service.getObjectDbid(capacityRule), it)

        setProperty("siteDBID", service.getObjectDbid(site), it)
        setProperty("contractDBID", service.getObjectDbid(contract), it)
    }

    fun updateTenantReferences() {
        managers?.forEach { it.tenant = tenant }
        routeDNs?.forEach { it.updateTenantReferences(tenant) }
        capacityTable?.tenant = tenant
        quotaTable?.tenant = tenant
        capacityRule?.tenant = tenant
        contract?.tenant = tenant
    }

    @JsonIgnore
    fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(tenant)
            .add(managers)
            .add(routeDNs)
            .add(capacityTable)
            .add(quotaTable)
            .add(capacityRule)
            .add(site)
            .add(contract)
            .toSet()
}
