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
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.nuecho.mutagen.cli.core.InitializingBean
import com.nuecho.mutagen.cli.getFolderReference
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.mutagen.cli.models.configuration.reference.DNReference
import com.nuecho.mutagen.cli.models.configuration.reference.FolderReference
import com.nuecho.mutagen.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.mutagen.cli.models.configuration.reference.PlaceReference
import com.nuecho.mutagen.cli.models.configuration.reference.ScriptReference
import com.nuecho.mutagen.cli.models.configuration.reference.TenantReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.toShortName

class Place(
    val tenant: TenantReference,
    val name: String,
    val dns: List<DNReference>? = null,
    val capacityRule: ScriptReference? = null,
    val contract: ObjectiveTableReference? = null,
    val site: FolderReference? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = PlaceReference(name, tenant)

    constructor(place: CfgPlace) : this(
        tenant = TenantReference(place.tenant.name),
        name = place.name,
        dns = place.dNs?.map { it.getReference() },
        capacityRule = place.capacityRule?.getReference(),
        contract = place.contract?.getReference(),
        site = place.site?.getReference(),
        state = place.state?.toShortName(),
        userProperties = place.userProperties?.asCategorizedProperties(),
        folder = place.getFolderReference()
    )

    override fun createCfgObject(service: ConfService) =
        updateCfgObject(service, CfgPlace(service)).also {
            setProperty("name", name, it)
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setFolder(folder, it, service)
        }

    override fun updateCfgObject(service: ConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgPlace).also {
            setProperty("DNDBIDs", dns?.map { service.getObjectDbid(it) } ?: emptyList<Int>(), it)
            setProperty("capacityRuleDBID", service.getObjectDbid(capacityRule), it)
            setProperty("contractDBID", service.getObjectDbid(contract), it)
            setProperty("siteDBID", service.getObjectDbid(site), it)
            setProperty("userProperties", ConfigurationObjects.toKeyValueCollection(userProperties), it)
            setProperty("state", ConfigurationObjects.toCfgObjectState(state), it)
        }

    override fun cloneBare() = Place(tenant, name)

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> = emptySet()

    override fun checkUnchangeableProperties(cfgObject: CfgObject) = checkUnchangeableProperties(this, cfgObject)

    override fun afterPropertiesSet() {
        dns?.forEach { it.updateTenantReferences(tenant) }
        capacityRule?.tenant = tenant
        contract?.tenant = tenant
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(tenant)
            .add(dns)
            .add(capacityRule)
            .add(contract)
            .add(site)
            .add(folder)
            .toSet()
}
