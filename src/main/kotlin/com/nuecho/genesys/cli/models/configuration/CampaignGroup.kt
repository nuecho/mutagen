package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgCampaignGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlaceGroup
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgDialMode
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgOperationMode
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgOptimizationMethod
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.AgentGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.ApplicationReference
import com.nuecho.genesys.cli.models.configuration.reference.CampaignGroupCampaignReference
import com.nuecho.genesys.cli.models.configuration.reference.CampaignGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.CampaignReference
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.GVPIVRProfileReference
import com.nuecho.genesys.cli.models.configuration.reference.GroupReference
import com.nuecho.genesys.cli.models.configuration.reference.PlaceGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

data class CampaignGroup(
    val campaign: CampaignGroupCampaignReference,
    val name: String,
    val description: String? = null,
    val dialMode: String? = null,
    val group: GroupReference? = null,
    val interactionQueue: ScriptReference? = null,
    val ivrProfile: GVPIVRProfileReference? = null,
    val maxQueueSize: Int? = null,
    val minRecBuffSize: Int? = null,
    val numOfChannels: Int? = null,
    val operationMode: String? = null,
    val optMethod: String? = null,
    val optMethodValue: Int? = null,
    val optRecBuffSize: Int? = null,
    val origDN: DNReference? = null,
    val script: ScriptReference? = null,
    val servers: List<ApplicationReference>? = null,
    val tenant: TenantReference? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {

    @get:JsonIgnore
    override val reference = CampaignGroupReference(campaign, name)

    constructor(campaignGroup: CfgCampaignGroup) : this(
        campaign = campaignGroup.campaign.run { CampaignGroupCampaignReference(name, tenant.getReference()) },
        name = campaignGroup.name,
        description = campaignGroup.description,
        dialMode = campaignGroup.dialMode?.toShortName(),
        group = campaignGroup.configurationService.retrieveObject(
            campaignGroup.groupType,
            campaignGroup.groupDBID
        )?.let {
            @Suppress("IMPLICIT_CAST_TO_ANY") // reported issue: https://youtrack.jetbrains.com/issue/KT-24458
            if (it is CfgAgentGroup) {
                AgentGroupReference(it.groupInfo.name, it.groupInfo.tenant.getReference())
            } else {
                it as CfgPlaceGroup
                PlaceGroupReference(it.groupInfo.name, it.groupInfo.tenant.getReference())
            } as GroupReference
        },
        interactionQueue = campaignGroup.interactionQueue?.getReference(),
        ivrProfile = campaignGroup.ivrProfile?.getReference(),
        maxQueueSize = campaignGroup.maxQueueSize,
        minRecBuffSize = campaignGroup.minRecBuffSize,
        numOfChannels = campaignGroup.numOfChannels,
        operationMode = campaignGroup.operationMode?.toShortName(),
        optMethod = campaignGroup.optMethod?.toShortName(),
        optMethodValue = campaignGroup.optMethodValue,
        optRecBuffSize = campaignGroup.optRecBuffSize,
        origDN = campaignGroup.origDN?.getReference(),
        script = campaignGroup.script?.getReference(),
        servers = campaignGroup.servers?.map { it.getReference() },
        tenant = campaignGroup.tenant?.getReference(),

        state = campaignGroup.state?.toShortName(),
        folder = campaignGroup.getFolderReference(),
        userProperties = campaignGroup.userProperties.asCategorizedProperties()
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgCampaignGroup(service))

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgCampaignGroup).also { cfgCampaignGroup ->
            setProperty("campaignDBID", campaign.let { service.getObjectDbid(it) }, cfgCampaignGroup)
            setProperty("name", name, cfgCampaignGroup)
            setProperty("description", description, cfgCampaignGroup)
            setProperty("dialMode", toCfgDialMode(dialMode), cfgCampaignGroup)
            setProperty(
                "groupDBID",
                service.getObjectDbid(
                    if (group is AgentGroupReference) group
                    else group as PlaceGroupReference?
                ),
                cfgCampaignGroup
            )
            setProperty("origDNDBID", service.getObjectDbid(origDN), cfgCampaignGroup)
            setProperty("interactionQueueDBID", service.getObjectDbid(interactionQueue), cfgCampaignGroup)
            setProperty("IVRProfileDBID", service.getObjectDbid(ivrProfile), cfgCampaignGroup)
            setProperty("maxQueueSize", maxQueueSize, cfgCampaignGroup)
            setProperty("minRecBuffSize", minRecBuffSize, cfgCampaignGroup)
            setProperty("numOfChannels", numOfChannels, cfgCampaignGroup)
            setProperty("operationMode", toCfgOperationMode(operationMode), cfgCampaignGroup)
            setProperty("optMethod", toCfgOptimizationMethod(optMethod), cfgCampaignGroup)
            setProperty("optMethodValue", optMethodValue, cfgCampaignGroup)
            setProperty("optRecBuffSize", optRecBuffSize, cfgCampaignGroup)
            setProperty(
                "scriptDBID",
                script?.let { service.getObjectDbid(it) },
                cfgCampaignGroup
            )
            setProperty(
                "serverDBIDs",
                servers?.map { service.getObjectDbid(it) },
                cfgCampaignGroup
            )

            setProperty("state", toCfgObjectState(state), cfgCampaignGroup)
            setProperty("userProperties", toKeyValueCollection(userProperties), cfgCampaignGroup)
            setFolder(folder, cfgCampaignGroup)
        }

    override fun cloneBare() = CampaignGroup(campaign = campaign, name = name, group = group)

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> =
        if (group == null) setOf(GROUP) else emptySet()

    override fun afterPropertiesSet() {
        group?.tenant = campaign.tenant
        interactionQueue?.tenant = campaign.tenant
        origDN?.run {
            tenant = campaign.tenant
            switch.tenant = campaign.tenant
        }
        script?.tenant = campaign.tenant
    }

    @Suppress("IMPLICIT_CAST_TO_ANY") // reported issue: https://youtrack.jetbrains.com/issue/KT-24458
    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(campaign.let { CampaignReference(it.primaryKey, it.tenant) })
            .add(
                when (group) {
                    is AgentGroupReference -> group
                    is PlaceGroupReference -> group
                    else -> null
                } as ConfigurationObjectReference<*>?
            )
            .add(interactionQueue)
            .add(ivrProfile)
            .add(origDN)
            .add(script)
            .add(servers)
            .add(folder)
            .toSet()
}
