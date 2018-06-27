package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAccessGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgID
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.configuration.protocol.types.CfgAccessGroupType
import com.genesyslab.platform.configuration.protocol.types.CfgAccessGroupType.CFGDefaultGroup
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGFolder
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPerson
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_SITE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_SITE_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgAccessGroup
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDN
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgObjectiveTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPerson
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgStatTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.StatTableReference
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.staticMockk
import io.mockk.use
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val NAME = "accessGroup"
private const val MEMBER1 = "member1"
private const val MEMBER2 = "member2"
private val accessGroup = AccessGroup(
    group = Group(
        tenant = DEFAULT_TENANT_REFERENCE,
        name = NAME,
        capacityTable = StatTableReference("capacityTable", DEFAULT_TENANT_REFERENCE),
        quotaTable = StatTableReference("quotaTable", DEFAULT_TENANT_REFERENCE),
        state = CfgObjectState.CFGEnabled.toShortName(),
        userProperties = ConfigurationTestData.defaultProperties(),
        capacityRule = ScriptReference("capacityRule", DEFAULT_TENANT_REFERENCE),
        site = DEFAULT_SITE_REFERENCE,
        contract = ObjectiveTableReference("contract", DEFAULT_TENANT_REFERENCE)
    ),
    members = listOf(
        PersonReference(MEMBER1, DEFAULT_TENANT_REFERENCE),
        PersonReference(MEMBER2, DEFAULT_TENANT_REFERENCE)
    ),
    type = CFGDefaultGroup.toShortName()
)

class AccessGroupTest : GroupConfigurationObjectTest(
    accessGroup,
    AccessGroup(tenant = DEFAULT_TENANT_REFERENCE, name = NAME)
) {

    @Test
    fun `CfgAccessGroup initialized Group should properly serialize`() {
        val service = mockConfService()
        val member1 = mockCfgPerson(MEMBER1)
        val member2 = mockCfgPerson(MEMBER2)

        objectMockk(ConfigurationObjects).use {
            every {
                service.retrieveObject(CFGPerson, any())
            } returns member1 andThen member2

            val accessGroup = AccessGroup(mockCfgAccessGroup(service))
            checkSerialization(accessGroup, "accessgroup")
        }
    }

    @Test
    fun `updateCfgObject should properly create CfgAccessGroup`() {
        val member = mockCfgPerson(MEMBER1)

        staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
            val service = mockConfService()
            every { service.retrieveObject(CfgPerson::class.java, any()) } answers { member }
            every { service.retrieveObject(CfgAccessGroup::class.java, any()) } returns null
            every { service.getObjectDbid(any()) } answers { DEFAULT_OBJECT_DBID }

            val cfgAccessGroup = accessGroup.updateCfgObject(service)

            val cfgId = CfgID(service, cfgAccessGroup)
            cfgId.dbid = DEFAULT_OBJECT_DBID
            cfgId.type = CFGPerson

            with(cfgAccessGroup) {
                assertEquals(type, CFGDefaultGroup)
                assertEquals(memberIDs.size, 2)
                assertEquals(memberIDs.toList()[0].dbid, DEFAULT_OBJECT_DBID)
                assertEquals(memberIDs.toList()[1].dbid, DEFAULT_OBJECT_DBID)
                assertEquals(memberIDs.toList()[0].type, CFGPerson)
                assertEquals(memberIDs.toList()[1].type, CFGPerson)

                with(groupInfo) {
                    assertEquals(name, accessGroup.group.name)
                    assertEquals(managerDBIDs, null)
                    assertEquals(routeDNDBIDs, null)
                    assertEquals(capacityTableDBID, DEFAULT_OBJECT_DBID)
                    assertEquals(quotaTableDBID, DEFAULT_OBJECT_DBID)
                    assertEquals(state, toCfgObjectState(accessGroup.group.state))
                    assertEquals(userProperties.asCategorizedProperties(), accessGroup.userProperties)
                    assertEquals(capacityRuleDBID, DEFAULT_OBJECT_DBID)
                    assertEquals(siteDBID, DEFAULT_OBJECT_DBID)
                    assertEquals(contractDBID, DEFAULT_OBJECT_DBID)
                }
            }
        }
    }
}

private fun mockCfgAccessGroup(service: IConfService): CfgAccessGroup {
    val cfgAccessGroup = mockCfgAccessGroup(accessGroup.group.name)

    val membersMock = accessGroup.members?.map { mockCfgID(CFGPerson) }
    val managersMock = accessGroup.group.managers?.map { ref -> mockCfgPerson(ref.primaryKey) }
    val cfgSwitch = mockCfgSwitch("switch")
    val routeDNsMock = accessGroup.group.routeDNs?.map { ref ->
        mockCfgDN(ref.number).apply {
            every { switch } returns cfgSwitch
            every { type } returns CfgDNType.CFGCP
            every { name } returns null
        }
    }
    val capacityTableMock = mockCfgStatTable(accessGroup.group.capacityTable!!.primaryKey)
    val quotaTableMock = mockCfgStatTable(accessGroup.group.quotaTable!!.primaryKey)
    val capacityRuleMock = mockCfgScript(accessGroup.group.capacityRule!!.primaryKey)
    val siteMock = mockCfgFolder(DEFAULT_SITE, CFGFolder)
    val contractMock = mockCfgObjectiveTable(accessGroup.group.contract!!.primaryKey)

    return cfgAccessGroup.apply {
        every { configurationService } returns service
        every { memberIDs } returns membersMock

        every { type } returns CfgAccessGroupType.CFGDefaultGroup
        every { groupInfo.managers } returns managersMock
        every { groupInfo.routeDNs } returns routeDNsMock
        every { groupInfo.capacityTable } returns capacityTableMock
        every { groupInfo.quotaTable } returns quotaTableMock
        every { groupInfo.userProperties } returns mockKeyValueCollection()
        every { groupInfo.capacityRule } returns capacityRuleMock
        every { groupInfo.site } returns siteMock
        every { groupInfo.state } returns CFGEnabled
        every { groupInfo.contract } returns contractMock
    }
}
