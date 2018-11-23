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
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgCallingListInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgCampaign
import com.nuecho.mutagen.cli.asBoolean
import com.nuecho.mutagen.cli.core.InitializingBean
import com.nuecho.mutagen.cli.getFolderReference
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.reference.CallingListReference
import com.nuecho.mutagen.cli.models.configuration.reference.CampaignReference
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.mutagen.cli.models.configuration.reference.FolderReference
import com.nuecho.mutagen.cli.models.configuration.reference.ScriptReference
import com.nuecho.mutagen.cli.models.configuration.reference.TenantReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.toShortName

/**
 * Not in use campaignGroups property is not defined.
 */
data class Campaign(
    val tenant: TenantReference,
    val name: String,
    val callingLists: List<CallingListInfo>? = null,
    val description: String? = null,
    val script: ScriptReference? = null,

    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = CampaignReference(name, tenant)

    constructor(campaign: CfgCampaign) : this(
        tenant = campaign.tenant.getReference(),
        name = campaign.name,
        callingLists = campaign.callingLists?.map { CallingListInfo(it) },
        description = campaign.description,
        script = campaign.script?.getReference(),

        state = campaign.state?.toShortName(),
        folder = campaign.getFolderReference(),
        userProperties = campaign.userProperties.asCategorizedProperties()
    )

    override fun createCfgObject(service: ConfService) =
        updateCfgObject(service, CfgCampaign(service)).also {
            setProperty("name", name, it)
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setFolder(folder, it, service)
        }

    override fun updateCfgObject(service: ConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgCampaign).also {
            setProperty("callingLists", toCfgCallingListList(callingLists, it, service), it)
            setProperty("description", description, it)
            setProperty("scriptDBID", service.getObjectDbid(script), it)
            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
        }

    override fun cloneBare() = Campaign(tenant, name)

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> = emptySet()

    override fun checkUnchangeableProperties(cfgObject: CfgObject) = checkUnchangeableProperties(this, cfgObject)

    override fun afterPropertiesSet() {
        script?.tenant = tenant
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(tenant)
            .add(callingLists?.map { it.callingList })
            .add(script)
            .add(folder)
            .toSet()
}

fun toCfgCallingListList(callingListInfos: List<CallingListInfo>?, campaign: CfgCampaign, service: ConfService) =
    if (callingListInfos == null) null
    else {
        callingListInfos.map {
            CfgCallingListInfo(service, campaign).apply {
                callingListDBID = service.getObjectDbid(it.callingList)
                isActive = toCfgFlag(it.isActive)
                share = it.share
            }
        }
    }

data class CallingListInfo(
    val callingList: CallingListReference,
    @get:JsonProperty("isActive")
    val isActive: Boolean? = null,
    val share: Int? = null
) {
    constructor(callingListInfo: CfgCallingListInfo) : this(
        callingList = callingListInfo.callingList.getReference(),
        isActive = callingListInfo.isActive?.asBoolean(),
        share = callingListInfo.share
    )
}
