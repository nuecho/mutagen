package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGCP
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDN
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgObjectiveTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPerson
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgStatTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.StatTableReference
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveDN
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveObjectiveTable
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrievePerson
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveScript
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveStatTable
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

private const val NAME = "name"
private val agentGroup = AgentGroup(
    agents = listOf(
        PersonReference("agent1"),
        PersonReference("agent2"),
        PersonReference("agent3")
    ),
    group = Group(
        tenant = DEFAULT_TENANT_REFERENCE,
        name = "name",
        managers = listOf(
            PersonReference("manager1"),
            PersonReference("manager2")
        ),
        routeDNs = listOf(
            DNReference(number = "123", switch = "switch", type = CFGCP),
            DNReference(number = "456", switch = "switch", type = CFGCP),
            DNReference(number = "789", switch = "switch", type = CFGCP)
        ),
        capacityTable = StatTableReference("capacityTable"),
        quotaTable = StatTableReference("quotaTable"),
        state = CfgObjectState.CFGEnabled.toShortName(),
        userProperties = ConfigurationTestData.defaultProperties(),
        capacityRule = ScriptReference("capacityRule"),
        site = FolderReference("site"),
        contract = ObjectiveTableReference("contract")
    )
)

class AgentGroupTest : ConfigurationObjectTest(
    agentGroup,
    AgentGroup(tenant = DEFAULT_TENANT_REFERENCE, name = NAME),
    AgentGroup(mockCfgAgentGroup())
) {
    init {
        val service = ServiceMocks.mockConfService()

        "AgentGroup.updateCfgObject should properly create CfgAgentGroup" {
            val cfgSwitch = mockCfgSwitch("switch")
            every { service.retrieveObject(CfgAgentGroup::class.java, any()) } returns null
            every { service.retrieveObject(CfgSwitch::class.java, any()) } returns cfgSwitch

            mockRetrieveTenant(service)
            mockRetrievePerson(service)
            mockRetrieveDN(service, cfgSwitch)
            mockRetrieveObjectiveTable(service)
            mockRetrieveStatTable(service)
            mockRetrieveFolder(service)
            mockRetrieveScript(service)

            val (status, cfgObject) = agentGroup.updateCfgObject(service)
            val cfgAgentGroup = cfgObject as CfgAgentGroup

            status shouldBe ConfigurationObjectUpdateStatus.CREATED

            with(cfgAgentGroup) {
                agentDBIDs shouldBe listOf(DEFAULT_OBJECT_DBID, DEFAULT_OBJECT_DBID, DEFAULT_OBJECT_DBID)

                with(groupInfo) {
                    name shouldBe agentGroup.group.name
                    managerDBIDs shouldBe listOf(DEFAULT_OBJECT_DBID, DEFAULT_OBJECT_DBID)
                    routeDNDBIDs shouldBe listOf(DEFAULT_OBJECT_DBID, DEFAULT_OBJECT_DBID, DEFAULT_OBJECT_DBID)
                    capacityTableDBID shouldBe DEFAULT_OBJECT_DBID
                    quotaTableDBID shouldBe DEFAULT_OBJECT_DBID
                    state shouldBe toCfgObjectState(agentGroup.group.state)
                    userProperties.asCategorizedProperties() shouldBe agentGroup.userProperties
                    capacityRuleDBID shouldBe DEFAULT_OBJECT_DBID
                    siteDBID shouldBe DEFAULT_OBJECT_DBID
                    contractDBID shouldBe DEFAULT_OBJECT_DBID
                }
            }
        }
    }
}

@Suppress("LongMethod")
private fun mockCfgAgentGroup(): CfgAgentGroup {
    val cfgSwitch = mockk<CfgSwitch>().apply { every { name } returns "switch" }

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
    val siteMock = mockCfgFolder(agentGroup.group.site!!.primaryKey)
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
