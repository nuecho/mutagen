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

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgCampaignGroup
import com.genesyslab.platform.applicationblocks.com.queries.CfgCampaignGroupQuery
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectNotFoundException
import com.nuecho.mutagen.cli.services.getObjectDbid

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
