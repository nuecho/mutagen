package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGCP
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_SITE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_SITE_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgAgentGroup
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDN
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgObjectiveTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPerson
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgStatTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.StatTableReference
import com.nuecho.genesys.cli.services.ConfServiceCache
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveDN
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveObjectiveTable
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrievePerson
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveScript
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveStatTable
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val NAME = "name"
private val agentGroup = AgentGroup(
    agents = listOf(
        PersonReference("agent1", DEFAULT_TENANT_REFERENCE),
        PersonReference("agent2", DEFAULT_TENANT_REFERENCE),
        PersonReference("agent3", DEFAULT_TENANT_REFERENCE)
    ),
    group = Group(
        tenant = DEFAULT_TENANT_REFERENCE,
        name = "name",
        managers = listOf(
            PersonReference("manager1", DEFAULT_TENANT_REFERENCE),
            PersonReference("manager2", DEFAULT_TENANT_REFERENCE)
        ),
        routeDNs = listOf(
            DNReference(number = "123", switch = "switch", type = CFGCP, tenant = DEFAULT_TENANT_REFERENCE),
            DNReference(number = "456", switch = "switch", type = CFGCP, tenant = DEFAULT_TENANT_REFERENCE),
            DNReference(number = "789", switch = "switch", type = CFGCP, tenant = DEFAULT_TENANT_REFERENCE)
        ),
        capacityTable = StatTableReference("capacityTable", DEFAULT_TENANT_REFERENCE),
        quotaTable = StatTableReference("quotaTable", DEFAULT_TENANT_REFERENCE),
        state = CfgObjectState.CFGEnabled.toShortName(),
        userProperties = ConfigurationTestData.defaultProperties(),
        capacityRule = ScriptReference("capacityRule", DEFAULT_TENANT_REFERENCE),
        site = DEFAULT_SITE_REFERENCE,
        contract = ObjectiveTableReference("contract", DEFAULT_TENANT_REFERENCE)
    )
)

class AgentGroupTest : ConfigurationObjectTest(
    agentGroup,
    AgentGroup(tenant = DEFAULT_TENANT_REFERENCE, name = NAME),
    AgentGroup(mockCfgAgentGroup())
) {
    val service = ServiceMocks.mockConfService()

    @Test
    fun `updateCfgObject should properly create CfgAgentGroup`() {
        val cfgSwitch = mockCfgSwitch("switch")
        every { service.retrieveObject(CfgAgentGroup::class.java, any()) } returns null
        every { service.retrieveObject(CfgSwitch::class.java, any()) } returns cfgSwitch

        mockRetrieveTenant(service)
        mockRetrievePerson(service)
        mockRetrieveDN(service, cfgSwitch)
        mockRetrieveObjectiveTable(service)
        mockRetrieveStatTable(service)
        mockRetrieveScript(service)

        objectMockk(ConfServiceCache).use {
            ConfServiceExtensionMocks.mockCfgFolderCache()
            val cfgAgentGroup = agentGroup.updateCfgObject(service)

            with(cfgAgentGroup) {
                assertThat(agentDBIDs, contains(DEFAULT_OBJECT_DBID, DEFAULT_OBJECT_DBID, DEFAULT_OBJECT_DBID))

                with(groupInfo) {
                    assertThat(name, equalTo(agentGroup.group.name))
                    assertThat(managerDBIDs, contains(DEFAULT_OBJECT_DBID, DEFAULT_OBJECT_DBID))
                    assertThat(routeDNDBIDs, contains(DEFAULT_OBJECT_DBID, DEFAULT_OBJECT_DBID, DEFAULT_OBJECT_DBID))
                    assertThat(capacityTableDBID, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(quotaTableDBID, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(state, equalTo(toCfgObjectState(agentGroup.group.state)))
                    assertThat(userProperties.asCategorizedProperties(), equalTo(agentGroup.userProperties))
                    assertThat(capacityRuleDBID, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(siteDBID, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(contractDBID, equalTo(DEFAULT_OBJECT_DBID))
                }
            }
        }
    }
}

@Suppress("LongMethod")
private fun mockCfgAgentGroup(): CfgAgentGroup {
    val cfgSwitch = mockCfgSwitch("switch")

    val agentsMock = agentGroup.agents?.map { ref -> mockCfgPerson(ref.primaryKey) }
    val managersMock = agentGroup.group.managers?.map { ref -> mockCfgPerson(ref.primaryKey) }
    val routeDNsMock = agentGroup.group.routeDNs?.map { ref ->
        mockCfgDN(ref.number).apply {
            every { switch } returns cfgSwitch
            every { type } returns CfgDNType.CFGCP
            every { name } returns null
        }
    }

    val capacityTableMock = mockCfgStatTable(agentGroup.group.capacityTable!!.primaryKey)
    val quotaTableMock = mockCfgStatTable(agentGroup.group.quotaTable!!.primaryKey)
    val capacityRuleMock = mockCfgScript(agentGroup.group.capacityRule!!.primaryKey)
    val siteMock = mockCfgFolder(DEFAULT_SITE, CFGFolder)
    val contractMock = mockCfgObjectiveTable(agentGroup.group.contract!!.primaryKey)

    return ConfigurationObjectMocks.mockCfgAgentGroup(agentGroup.group.name).apply {
        every { agents } returns agentsMock
        every { groupInfo.managers } returns managersMock
        every { groupInfo.routeDNs } returns routeDNsMock
        every { groupInfo.capacityTable } returns capacityTableMock
        every { groupInfo.quotaTable } returns quotaTableMock
        every { groupInfo.state } returns ConfigurationObjects.toCfgObjectState(agentGroup.group.state)
        every { groupInfo.userProperties } returns ConfigurationObjectMocks.mockKeyValueCollection()
        every { groupInfo.capacityRule } returns capacityRuleMock
        every { groupInfo.site } returns siteMock
        every { groupInfo.contract } returns contractMock
    }
}
