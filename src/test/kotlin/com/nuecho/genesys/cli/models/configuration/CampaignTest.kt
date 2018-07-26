package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgCampaign
import com.genesyslab.platform.configuration.protocol.types.CfgFlag.CFGTrue
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgCallingList
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgCallingListInfo
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgCampaign
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.CallingListReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveCallingList
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveScript
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val CALLING_LIST_NAME = "callingList"
private const val CAMPAIGN_NAME = "campaign"
private const val CALLING_LIST_DBID = 102
private const val SCRIPT_DBID = 103
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
    campaign,
    Campaign(tenant = DEFAULT_TENANT_REFERENCE, name = campaign.name),
    emptySet()
) {
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

        every { service.retrieveObject(CfgCampaign::class.java, any()) } returns null
        mockRetrieveCallingList(service, CALLING_LIST_DBID)
        mockRetrieveScript(service, SCRIPT_DBID)
        mockRetrieveTenant(service)

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgCampaign = campaign.createCfgObject(service)

            with(cfgCampaign) {
                assertThat(callingLists.toList()[0].callingListDBID, equalTo(CALLING_LIST_DBID))
                assertThat(description, equalTo(campaign.description))
                assertThat(name, equalTo(campaign.name))
                assertThat(scriptDBID, equalTo(SCRIPT_DBID))

                assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
                assertThat(state, equalTo(toCfgObjectState(campaign.state)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(campaign.userProperties))
            }
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
        every { folderId } returns DEFAULT_OBJECT_DBID
    }
}
