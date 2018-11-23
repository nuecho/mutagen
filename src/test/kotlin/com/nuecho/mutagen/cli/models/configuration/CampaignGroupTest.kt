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

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgCampaignGroup
import com.genesyslab.platform.configuration.protocol.types.CfgDialMode.CFGDMPredict
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGAgentGroup
import com.genesyslab.platform.configuration.protocol.types.CfgOperationMode.CFGOMManual
import com.genesyslab.platform.configuration.protocol.types.CfgOptimizationMethod.CFGOMOverdialRate
import com.nuecho.mutagen.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_NAME
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgAgentGroup
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgApplication
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgCampaignGroup
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgDN
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgGVPIVRProfile
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgDNType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.reference.AgentGroupReference
import com.nuecho.mutagen.cli.models.configuration.reference.ApplicationReference
import com.nuecho.mutagen.cli.models.configuration.reference.CampaignGroupCampaignReference
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.mutagen.cli.models.configuration.reference.DNReference
import com.nuecho.mutagen.cli.models.configuration.reference.GVPIVRProfileReference
import com.nuecho.mutagen.cli.models.configuration.reference.ScriptReference
import com.nuecho.mutagen.cli.models.configuration.reference.SwitchReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveAgentGroup
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveApplication
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveCampaign
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.mutagen.cli.services.ServiceMocks.mockConfService
import com.nuecho.mutagen.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val AGENT_GROUP_DBID = 108
private const val CAMPAIGN_DBID = 109

private const val CAMPAIGN = "campaign"
private val CAMPAIGN_REFERENCE = CampaignGroupCampaignReference(CAMPAIGN, DEFAULT_TENANT_REFERENCE)
private const val SCRIPT1 = "script1"
private const val SCRIPT2 = "script2"
private val DIAL_MODE = CFGDMPredict
private val ENABLED_STATE = CFGEnabled
private val OPERATION_MODE = CFGOMManual
private val OPT_METHOD = CFGOMOverdialRate

private val campaignGroup = CampaignGroup(
    name = "campaignGroup",
    campaign = CAMPAIGN_REFERENCE,
    description = "description",
    dialMode = DIAL_MODE.toShortName(),
    group = AgentGroupReference("group", DEFAULT_TENANT_REFERENCE),
    interactionQueue = ScriptReference(SCRIPT1, DEFAULT_TENANT_REFERENCE),
    ivrProfile = GVPIVRProfileReference("ivrProfile"),
    maxQueueSize = 1,
    minRecBuffSize = 4,
    numOfChannels = 1,
    operationMode = OPERATION_MODE.toShortName(),
    optMethod = OPT_METHOD.toShortName(),
    optMethodValue = 1,
    optRecBuffSize = 6,
    origDN = DNReference(
        number = "dn",
        switch = SwitchReference("switch", DEFAULT_TENANT_REFERENCE),
        type = "acdqueue", // cfgACDQueue cfgRoutingPoint
        tenant = DEFAULT_TENANT_REFERENCE
    ),
    script = ScriptReference(SCRIPT2, DEFAULT_TENANT_REFERENCE),
    servers = listOf(ApplicationReference("applicationServer")),
    tenant = DEFAULT_TENANT_REFERENCE,
    state = ENABLED_STATE.toShortName(),
    userProperties = ConfigurationTestData.defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class CampaignGroupTest : ConfigurationObjectTest(
    configurationObject = campaignGroup,
    emptyConfigurationObject = CampaignGroup(campaign = CAMPAIGN_REFERENCE, name = campaignGroup.name),
    mandatoryProperties = setOf(GROUP)
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(campaignGroup.campaign.toCampaignReference())
            .add(campaignGroup.group as ConfigurationObjectReference<*>)
            .add(campaignGroup.interactionQueue)
            .add(campaignGroup.ivrProfile)
            .add(campaignGroup.origDN)
            .add(campaignGroup.script)
            .add(campaignGroup.servers)
            .add(campaignGroup.tenant)
            .add(campaignGroup.folder)
            .toSet()

        assertThat(campaignGroup.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        assertUnchangeableProperties(mockCfgCampaignGroup(mockConfService()), FOLDER)

    @Test
    override fun `initialized object should properly serialize`() {
        val service = mockConfService()
        val agentGroup = mockCfgAgentGroup("group")
        mockRetrieveFolderByDbid(service)

        objectMockk(ConfigurationObjects).use {
            every { service.retrieveObject(CFGAgentGroup, AGENT_GROUP_DBID) } returns agentGroup

            val campaignGroup = CampaignGroup(mockCfgCampaignGroup(service))
            checkSerialization(campaignGroup, "campaigngroup")
        }
    }

    @Test
    fun `createCfgObject should properly create CfgCampaignGroup`() {
        val service = mockConfService()
        val origDnDbid = 110
        val ivrProfileDbid = 111
        val script1Dbid = 112
        val script2Dbid = 113
        val applicationDbid = 114

        mockRetrieveTenant(service)
        mockRetrieveAgentGroup(service, AGENT_GROUP_DBID)
        mockRetrieveApplication(service, applicationDbid)
        mockRetrieveCampaign(service, CAMPAIGN_DBID)

        every { service.getObjectDbid(campaignGroup.interactionQueue) } returns script1Dbid
        every { service.getObjectDbid(campaignGroup.ivrProfile) } returns ivrProfileDbid
        every { service.getObjectDbid(campaignGroup.origDN) } returns origDnDbid
        every { service.getObjectDbid(campaignGroup.script) } returns script2Dbid

        val cfgCampaignGroup = campaignGroup.createCfgObject(service)

        with(cfgCampaignGroup) {
            assertThat(campaignDBID, equalTo(CAMPAIGN_DBID))
            assertThat(interactionQueueDBID, equalTo(script1Dbid))
            assertThat(ivrProfileDBID, equalTo(ivrProfileDbid))
            assertThat(groupDBID, equalTo(AGENT_GROUP_DBID))
            assertThat(origDNDBID, equalTo(origDnDbid))
            assertThat(scriptDBID, equalTo(script2Dbid))
            assertThat(serverDBIDs.toList(), equalTo(listOf(applicationDbid)))

            assertThat(campaignDBID, equalTo(CAMPAIGN_DBID))
            assertThat(description, equalTo(campaignGroup.description))
            assertThat(dialMode, equalTo(CFGDMPredict))
            assertThat(maxQueueSize, equalTo(campaignGroup.maxQueueSize))
            assertThat(minRecBuffSize, equalTo(campaignGroup.minRecBuffSize))
            assertThat(numOfChannels, equalTo(campaignGroup.numOfChannels))
            assertThat(operationMode, equalTo(OPERATION_MODE))
            assertThat(optMethod, equalTo(OPT_METHOD))
            assertThat(optMethodValue, equalTo(campaignGroup.optMethodValue))
            assertThat(optRecBuffSize, equalTo(campaignGroup.optRecBuffSize))

            assertThat(name, equalTo(campaignGroup.name))
            assertThat(state, equalTo(toCfgObjectState(campaignGroup.state)))
            assertThat(userProperties.asCategorizedProperties(), equalTo(campaignGroup.userProperties))
        }
    }
}

private fun mockCfgCampaignGroup(service: IConfService): CfgCampaignGroup {
    val cfgCampaignGroup = mockCfgCampaignGroup(campaignGroup.name)
    val cfgSwitch = mockCfgSwitch("switch")

    val mockInteractionQueue = mockCfgScript(SCRIPT1)
    val mockScript = mockCfgScript(SCRIPT2)
    val mockIvrProfile = mockCfgGVPIVRProfile(campaignGroup.ivrProfile!!.primaryKey)
    val mockOrigDN = mockCfgDN(campaignGroup.origDN!!.number, toCfgDNType(campaignGroup.origDN!!.type)!!)
        .apply {
            every { name } returns campaignGroup.origDN!!.name
            every { switch } returns cfgSwitch
        }
    val mockServer = mockCfgApplication(campaignGroup.servers!![0].primaryKey)
    val mockTenant = mockCfgTenant(DEFAULT_TENANT_NAME)

    return cfgCampaignGroup.apply {
        every { configurationService } returns service
        every { folderId } returns DEFAULT_FOLDER_DBID
        every { name } returns campaignGroup.name
        every { campaignDBID } returns DEFAULT_OBJECT_DBID
        every { description } returns campaignGroup.description
        every { dialMode } returns CFGDMPredict
        every { groupDBID } returns AGENT_GROUP_DBID
        every { groupType } returns CFGAgentGroup
        every { interactionQueue } returns mockInteractionQueue
        every { ivrProfile } returns mockIvrProfile
        every { maxQueueSize } returns campaignGroup.maxQueueSize
        every { minRecBuffSize } returns campaignGroup.minRecBuffSize
        every { numOfChannels } returns campaignGroup.numOfChannels
        every { operationMode } returns OPERATION_MODE
        every { optMethod } returns OPT_METHOD
        every { optMethodValue } returns campaignGroup.optMethodValue
        every { optRecBuffSize } returns campaignGroup.optRecBuffSize
        every { origDN } returns mockOrigDN
        every { script } returns mockScript
        every { servers } returns listOf(mockServer)
        every { state } returns ENABLED_STATE
        every { tenant } returns mockTenant
        every { userProperties } returns mockKeyValueCollection()
    }
}
