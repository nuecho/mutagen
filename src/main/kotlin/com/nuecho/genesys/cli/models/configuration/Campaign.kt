package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgCallingListInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgCampaign
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.CallingListReference
import com.nuecho.genesys.cli.models.configuration.reference.CampaignReference
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

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

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgCampaign(service))

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgCampaign).also {
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("name", name, it)
            setProperty("callingLists", toCfgCallingListList(callingLists, it), it)
            setProperty("description", description, it)
            setProperty("scriptDBID", service.getObjectDbid(script), it)

            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            setFolder(folder, it)
        }

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

fun toCfgCallingListList(callingListInfos: List<CallingListInfo>?, campaign: CfgCampaign) =
    if (callingListInfos == null) null
    else {
        val service = campaign.configurationService
        callingListInfos.map {
            val cfgCallingListInfo = CfgCallingListInfo(service, campaign)

            setProperty("callingListDBID", service.getObjectDbid(it.callingList), cfgCallingListInfo)
            setProperty("isActive", it.isActive, cfgCallingListInfo)
            setProperty("share", it.share, cfgCallingListInfo)

            cfgCallingListInfo
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
        isActive = callingListInfo.isActive.asBoolean(),
        share = callingListInfo.share
    )
}
