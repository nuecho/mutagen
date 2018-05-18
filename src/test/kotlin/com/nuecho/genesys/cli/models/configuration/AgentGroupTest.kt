package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGCP
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDN
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgObjectiveTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPerson
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgStatTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.StatTableReference
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks
import com.nuecho.genesys.cli.services.ServiceMocks
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.staticMockk
import io.mockk.use

private const val name = "name"
private val agentGroup = AgentGroup(
    agents = listOf(
        PersonReference("agent1"),
        PersonReference("agent2"),
        PersonReference("agent3")
    ),
    group = Group(
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

class AgentGroupTest : ConfigurationObjectTest(agentGroup, AgentGroup(name), AgentGroup(mockCfgAgentGroup())) {
    init {
        val service = ServiceMocks.mockConfService()

        "AgentGroup.updateCfgObject should properly create CfgAgentGroup" {
            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {

                val dbid = 101
                val cfgSwitch = mockk<CfgSwitch>().apply {
                    every { name } returns "switch"
                    every { objectDbid } returns dbid
                }

                every { service.retrieveObject(CfgSwitch::class.java, any()) } returns cfgSwitch
                every { service.retrieveObject(CfgAgentGroup::class.java, any()) } returns null
                ConfServiceExtensionMocks.mockRetrievePerson(service, dbid)
                ConfServiceExtensionMocks.mockRetrieveDN(service, dbid, cfgSwitch)
                ConfServiceExtensionMocks.mockRetrieveObjectiveTable(service, dbid)
                ConfServiceExtensionMocks.mockRetrieveStatTable(service, dbid)
                ConfServiceExtensionMocks.mockRetrieveFolder(service, dbid)
                ConfServiceExtensionMocks.mockRetrieveScript(service, dbid)

                val (status, cfgObject) = agentGroup.updateCfgObject(service)
                val cfgAgentGroup = cfgObject as CfgAgentGroup

                status shouldBe ConfigurationObjectUpdateStatus.CREATED

                with(cfgAgentGroup) {
                    agentDBIDs shouldBe listOf(dbid, dbid, dbid)

                    with(groupInfo) {
                        name shouldBe agentGroup.group.name
                        managerDBIDs shouldBe listOf(dbid, dbid)
                        routeDNDBIDs shouldBe listOf(dbid, dbid, dbid)
                        capacityTableDBID shouldBe dbid
                        quotaTableDBID shouldBe dbid
                        state shouldBe toCfgObjectState(agentGroup.group.state)
                        userProperties.asCategorizedProperties() shouldBe agentGroup.userProperties
                        capacityRuleDBID shouldBe dbid
                        siteDBID shouldBe dbid
                        contractDBID shouldBe dbid
                    }
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

    val groupMock = mockk<CfgGroup>().apply {
        every { name } returns agentGroup.group.name
        every { managers } returns managersMock
        every { routeDNs } returns routeDNsMock
        every { capacityTable } returns capacityTableMock
        every { quotaTable } returns quotaTableMock
        every { state } returns ConfigurationObjects.toCfgObjectState(agentGroup.group.state)
        every { userProperties } returns ConfigurationObjectMocks.mockKeyValueCollection()
        every { capacityRule } returns capacityRuleMock
        every { site } returns siteMock
        every { contract } returns contractMock
    }

    val cfgAgentGroup = mockk<CfgAgentGroup>()
    every { cfgAgentGroup.agents } returns agentsMock
    every { cfgAgentGroup.groupInfo } returns groupMock

    return cfgAgentGroup
}
