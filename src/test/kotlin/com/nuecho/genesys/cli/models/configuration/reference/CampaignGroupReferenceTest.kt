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

import com.genesyslab.platform.applicationblocks.com.objects.CfgCampaign
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks
import com.nuecho.genesys.cli.services.getObjectDbid
import io.mockk.every
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val CAMPAIGN = "campaign"
private val CAMPAIGN_REFERENCE = CampaignGroupCampaignReference(CAMPAIGN, DEFAULT_TENANT_REFERENCE)
private const val DBID = 102
private const val NAME = "campaignGroup"

class CampaignGroupReferenceTest {
    private val campaignGroupReference = CampaignGroupReference(
        name = NAME,
        campaign = CAMPAIGN_REFERENCE
    )

    @Test
    fun `CampaignGroupReference should be serialized as a JSON Object without tenant references`() {
        checkSerialization(campaignGroupReference, "reference/campaign_group_reference")
    }

    @Test
    fun `CampaignGroupReference should properly deserialize`() {
        val deserializedCampaignGroupReference = loadJsonConfiguration(
            "models/configuration/reference/campaign_group_reference.json",
            CampaignGroupReference::class.java
        )
        val expectedCampaignGroupReference = CampaignGroupReference(
            CampaignGroupCampaignReference(CAMPAIGN, DEFAULT_TENANT_REFERENCE),
            NAME
        )

        assertThat(deserializedCampaignGroupReference, equalTo(expectedCampaignGroupReference))
    }

    @Test
    fun `CampaignGroupReference toQuery should create the proper query`() {
        val service = ServiceMocks.mockConfService()
        mockRetrieveTenant(service)
        every { service.getObjectDbid(CAMPAIGN_REFERENCE) } returns DBID
        every { service.retrieveObject(CAMPAIGN_REFERENCE.cfgObjectClass, any()) } answers { CfgCampaign(service).apply { dbid = DBID } }

        val query = campaignGroupReference.toQuery(service)
        assertThat(query.campaignDbid, equalTo(DBID))
        assertThat(query.name, equalTo(NAME))
    }

    @Test
    fun `CampaignGroupReference toString() should generate the proper String`() {
        assertThat("name: '$NAME', campaign: '$CAMPAIGN_REFERENCE'", equalTo(campaignGroupReference.toString()))
    }

    @Test
    fun `CampaignGroupReference should be sorted properly`() {
        val campaignGroupReference1 = CampaignGroupReference(
            campaign = CampaignGroupCampaignReference(NAME, TenantReference("aaaTenant")),
            name = "aaa"
        )

        val campaignGroupReference2 = CampaignGroupReference(
            campaign = CampaignGroupCampaignReference(NAME, DEFAULT_TENANT_REFERENCE),
            name = "aaa"
        )
        val campaignGroupReference3 = CampaignGroupReference(
            campaign = CampaignGroupCampaignReference("${NAME}aaa", DEFAULT_TENANT_REFERENCE),
            name = "aaa"
        )

        val campaignGroupReference4 = CampaignGroupReference(
            campaign = CampaignGroupCampaignReference("${NAME}aaa", DEFAULT_TENANT_REFERENCE),
            name = "ccc"
        )

        val campaignGroupReference5 = CampaignGroupReference(
            campaign = CampaignGroupCampaignReference("${NAME}bbb", DEFAULT_TENANT_REFERENCE),
            name = "ccc"
        )

        val sortedList = listOf(
            campaignGroupReference5,
            campaignGroupReference4,
            campaignGroupReference3,
            campaignGroupReference2,
            campaignGroupReference1
        ).sorted()
        assertThat(
            sortedList, contains(
                campaignGroupReference1,
                campaignGroupReference2,
                campaignGroupReference3,
                campaignGroupReference4,
                campaignGroupReference5
            )
        )
    }
}
