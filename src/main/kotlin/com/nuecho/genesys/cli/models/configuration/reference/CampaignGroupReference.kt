package com.nuecho.genesys.cli.models.configuration.reference

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgCampaignGroup
import com.genesyslab.platform.applicationblocks.com.queries.CfgCampaignGroupQuery
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectNotFoundException
import com.nuecho.genesys.cli.services.getObjectDbid

data class CampaignGroupReference(
    val campaign: CampaignGroupCampaignReference,
    val name: String
) : ConfigurationObjectReference<CfgCampaignGroup>(CfgCampaignGroup::class.java) {

    override fun toQuery(service: IConfService) = CfgCampaignGroupQuery().also {
        it.name = name
        it.campaignDbid = service.getObjectDbid(campaign) ?: throw ConfigurationObjectNotFoundException(campaign)
    }

    override fun compareTo(other: ConfigurationObjectReference<*>): Int {
        if (other !is CampaignGroupReference) return super.compareTo(other)

        return Comparator
            .comparing(CampaignGroupReference::campaign)
            .thenComparing(CampaignGroupReference::name)
            .compare(this, other)
    }

    override fun toString() = "name: '$name', campaign: '$campaign'"
}
