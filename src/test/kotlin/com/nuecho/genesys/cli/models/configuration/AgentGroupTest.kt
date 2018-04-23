package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.objects.CfgGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgStatTable
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks
import com.nuecho.genesys.cli.services.retrieveAgentGroup
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.staticMockk
import io.mockk.use

private const val name = "name"
private val agentGroup = AgentGroup(
    listOf("agent1", "agent2", "agent3"),
    Group(
        name = "name",
        managers = listOf("manager1", "manager2"),
        routeDNs = listOf("123", "456", "789"),
        capacityTable = "capacityTable",
        quotaTable = "quotaTable",
        state = CfgObjectState.CFGEnabled.toShortName(),
        userProperties = ConfigurationTestData.defaultProperties(),
        capacityRule = "capacityRule",
        site = "site",
        contract = "contract"
    )
)

class AgentGroupTest : ConfigurationObjectTest(agentGroup, AgentGroup(name)) {

    init {
        "CfgAgentGroup initialized AgentGroup should properly serialize" {
            val agentGroup = AgentGroup(mockCfgAgentGroup())
            ConfigurationAsserts.checkSerialization(agentGroup, "agentgroup")
        }

        "AgentGroup.updateCfgObject should properly create CfgAgentGroup" {
            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {

                val dbid = 101
                every { service.retrieveAgentGroup(any()) } returns null
                ConfServiceExtensionMocks.mockRetrievePerson(service, dbid)
                ConfServiceExtensionMocks.mockRetrieveDN(service, dbid)
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
                        userProperties.size shouldBe 4
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
    val agentsMock = agentGroup.agents?.map { employeeID ->
        mockk<CfgPerson>().also { every { it.employeeID } returns employeeID }
    }
    val managersMock = agentGroup.group.managers?.map { employeeID ->
        mockk<CfgPerson>().also { every { it.employeeID } returns employeeID }
    }
    val routeDNsMock = agentGroup.group.routeDNs?.map { dn ->
        mockk<CfgDN>().also { every { it.number } returns dn }
    }
    val capacityTableMock = mockk<CfgStatTable>().also { every { it.name } returns agentGroup.group.capacityTable }
    val quotaTableMock = mockk<CfgStatTable>().also { every { it.name } returns agentGroup.group.quotaTable }
    val capacityRuleMock = mockk<CfgScript>().also { every { it.name } returns agentGroup.group.capacityRule }
    val siteMock = mockk<CfgFolder>().also { every { it.name } returns agentGroup.group.site }
    val contractMock = mockk<CfgObjectiveTable>().also { every { it.name } returns agentGroup.group.contract }

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
