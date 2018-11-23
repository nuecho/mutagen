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

import com.genesyslab.platform.applicationblocks.com.objects.CfgCampaign
import com.genesyslab.platform.configuration.protocol.types.CfgFlag.CFGTrue
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.mutagen.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgCallingList
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgCallingListInfo
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgCampaign
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.mutagen.cli.models.configuration.reference.CallingListReference
import com.nuecho.mutagen.cli.models.configuration.reference.ScriptReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveCallingList
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveScript
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.mutagen.cli.services.ServiceMocks.mockConfService
import com.nuecho.mutagen.cli.toShortName
import io.mockk.every
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val CALLING_LIST_NAME = "callingList"
private const val CAMPAIGN_NAME = "campaign"
private val campaign = Campaign(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = CAMPAIGN_NAME,
    callingLists = listOf(CallingListInfo(CallingListReference(CALLING_LIST_NAME), true, 10)),
    description = "description",
    script = ScriptReference("script", DEFAULT_TENANT_REFERENCE),

    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class CampaignTest : ConfigurationObjectTest(
    configurationObject = campaign,
    emptyConfigurationObject = Campaign(tenant = DEFAULT_TENANT_REFERENCE, name = campaign.name),
    mandatoryProperties = emptySet()
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(campaign.tenant)
            .add(campaign.callingLists!![0].callingList)
            .add(campaign.script)
            .add(campaign.folder)
            .toSet()

        assertThat(campaign.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        assertUnchangeableProperties(mockCfgCampaign(), FOLDER)

    @Test
    override fun `initialized object should properly serialize`() {
        val service = mockConfService()

        mockRetrieveFolderByDbid(service)
        mockRetrieveCallingList(service)
        mockRetrieveScript(service)
        mockRetrieveTenant(service)

        val campaign = Campaign(mockCfgCampaign())
        checkSerialization(campaign, CAMPAIGN_NAME)
    }

    @Test
    fun `createCfgObject should properly create CfgCampaign`() {
        val service = mockConfService()
        val callingListDbid = 102
        val scriptDbid = 103

        every { service.retrieveObject(CfgCampaign::class.java, any()) } returns null
        mockRetrieveCallingList(service, callingListDbid)
        mockRetrieveScript(service, scriptDbid)
        mockRetrieveTenant(service)

        val cfgCampaign = campaign.createCfgObject(service)

        with(cfgCampaign) {
            assertThat(callingLists.toList()[0].callingListDBID, equalTo(callingListDbid))
            assertThat(description, equalTo(campaign.description))
            assertThat(name, equalTo(campaign.name))
            assertThat(scriptDBID, equalTo(scriptDbid))

            assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
            assertThat(state, equalTo(toCfgObjectState(campaign.state)))
            assertThat(userProperties.asCategorizedProperties(), equalTo(campaign.userProperties))
        }
    }
}

private fun mockCfgCampaign(): CfgCampaign {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    val cfgCampaign = mockCfgCampaign(campaign.name)
    val mockScript = mockCfgScript("script")
    val mockCallingList = mockCfgCallingList(CALLING_LIST_NAME)
    val mockCallingListInfo = mockCfgCallingListInfo(callingList = mockCallingList, isActive = CFGTrue, share = 10)

    return cfgCampaign.apply {
        every { callingLists } returns listOf(mockCallingListInfo)
        every { configurationService } returns service
        every { description } returns campaign.description
        every { name } returns campaign.name
        every { script } returns mockScript
        every { state } returns toCfgObjectState(campaign.state)
        every { userProperties } returns mockKeyValueCollection()
        every { folderId } returns DEFAULT_FOLDER_DBID
    }
}
