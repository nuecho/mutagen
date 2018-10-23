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

package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlaceGroup
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPlace
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.PlaceGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.PlaceReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.getObjectDbid

data class PlaceGroup(
    val group: Group,
    val places: List<PlaceReference>? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = PlaceGroupReference(group.name, group.tenant)

    override val userProperties: CategorizedProperties?
        @JsonIgnore
        get() = group.userProperties

    constructor(placeGroup: CfgPlaceGroup) : this(
        group = Group(placeGroup.groupInfo),
        places = placeGroup.placeDBIDs?.mapNotNull {
            placeGroup.configurationService.retrieveObject(CFGPlace, it) as CfgPlace
        }?.map { it.getReference() },
        folder = placeGroup.getFolderReference()
    )

    constructor(tenant: TenantReference, name: String) : this(
        group = Group(tenant, name)
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgPlaceGroup(service).also {
            setFolder(folder, it)
        })

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgPlaceGroup).also {
            val groupInfo = group.toUpdatedCfgGroup(service, it.groupInfo ?: CfgGroup(service, it)).also { cfgGroup ->
                it.dbid?.let { dbid -> cfgGroup.dbid = dbid }
            }

            setProperty("groupInfo", groupInfo, it)
            setProperty("placeDBIDs", places?.map { place -> service.getObjectDbid(place) }, it)
        }

    override fun cloneBare() = PlaceGroup(Group(group.tenant, group.name))

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> = emptySet()

    override fun checkUnchangeableProperties(cfgObject: CfgObject) = checkUnchangeableProperties(this, cfgObject)

    override fun afterPropertiesSet() {
        group.updateTenantReferences()
        places?.forEach { it.tenant = group.tenant }
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(places)
            .add(group.getReferences())
            .add(folder)
            .toSet()
}
