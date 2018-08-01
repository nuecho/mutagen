package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAccessGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgID
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.configuration.protocol.types.CfgAccessGroupType
import com.genesyslab.platform.configuration.protocol.types.CfgAccessGroupType.CFGAdministratorsGroup
import com.genesyslab.platform.configuration.protocol.types.CfgAccessGroupType.CFGDefaultGroup
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGFolder
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPerson
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
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
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.StatTableReference
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.staticMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.nullValue
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
        userProperties = defaultProperties(),
        capacityRule = ScriptReference("capacityRule", DEFAULT_TENANT_REFERENCE),
        site = DEFAULT_FOLDER_REFERENCE,
        contract = ObjectiveTableReference("contract", DEFAULT_TENANT_REFERENCE)
    ),
    members = listOf(
        PersonReference(MEMBER1, DEFAULT_TENANT_REFERENCE),
        PersonReference(MEMBER2, DEFAULT_TENANT_REFERENCE)
    ),
    type = CFGDefaultGroup.toShortName(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class AccessGroupTest : ConfigurationObjectTest(
    configurationObject = accessGroup,
    emptyConfigurationObject = AccessGroup(tenant = DEFAULT_TENANT_REFERENCE, name = NAME),
    mandatoryProperties = emptySet()
) {

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() {
        val cfgAccessGroup = mockCfgAccessGroup().also {
            every { it.type } returns CFGAdministratorsGroup
        }

        assertThat(configurationObject.checkUnchangeableProperties(cfgAccessGroup), equalTo(setOf(TYPE)))
    }

    @Test
    override fun `initialized object should properly serialize`() {
        val service = mockConfService()
        val folder = mockCfgFolder()
        val member1 = mockCfgPerson(MEMBER1)
        val member2 = mockCfgPerson(MEMBER2)

        objectMockk(ConfigurationObjects).use {
            every { service.retrieveObject(CFGFolder, any()) } returns folder
            every {
                service.retrieveObject(CFGPerson, any())
            } returns member1 andThen member2

            val accessGroup = AccessGroup(mockAccessGroup(service))
            checkSerialization(accessGroup, "accessgroup")
        }
    }

    @Test
    fun `createCfgObject should properly create CfgAccessGroup`() {
        val member = mockCfgPerson(MEMBER1)
        val service = mockConfService()

        every { service.retrieveObject(CfgPerson::class.java, any()) } answers { member }
        every { service.retrieveObject(CfgAccessGroup::class.java, any()) } returns null

        staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
            every { service.getObjectDbid(any()) } answers { DEFAULT_OBJECT_DBID }

            objectMockk(ConfigurationObjectRepository).use {
                mockConfigurationObjectRepository()
                val cfgAccessGroup = accessGroup.createCfgObject(service)

                val cfgId = CfgID(service, cfgAccessGroup)
                cfgId.dbid = DEFAULT_OBJECT_DBID
                cfgId.type = CFGPerson

                with(cfgAccessGroup) {
                    assertThat(type, equalTo(CFGDefaultGroup))
                    assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
                    assertThat(memberIDs, hasSize(2))
                    assertThat(memberIDs.toList()[0].dbid, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(memberIDs.toList()[1].dbid, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(memberIDs.toList()[0].type, equalTo(CFGPerson))
                    assertThat(memberIDs.toList()[1].type, equalTo(CFGPerson))

                    assertThat(groupInfo.name, equalTo(accessGroup.group.name))
                    assertThat(groupInfo.managerDBIDs, `is`(nullValue()))
                    assertThat(groupInfo.routeDNDBIDs, `is`(nullValue()))
                    assertThat(groupInfo.capacityTableDBID, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(groupInfo.quotaTableDBID, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(groupInfo.state, equalTo(toCfgObjectState(accessGroup.group.state)))
                    assertThat(groupInfo.userProperties.asCategorizedProperties(), equalTo(accessGroup.userProperties))
                    assertThat(groupInfo.capacityRuleDBID, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(groupInfo.siteDBID, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(groupInfo.contractDBID, equalTo(DEFAULT_OBJECT_DBID))
                }
            }
        }
    }
}

private fun mockAccessGroup(service: IConfService): CfgAccessGroup {
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
    val siteMock = mockCfgFolder(DEFAULT_FOLDER, CFGFolder)
    val contractMock = mockCfgObjectiveTable(accessGroup.group.contract!!.primaryKey)

    return cfgAccessGroup.apply {
        every { configurationService } returns service
        every { memberIDs } returns membersMock
        every { type } returns CfgAccessGroupType.CFGDefaultGroup
        every { folderId } returns DEFAULT_OBJECT_DBID

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
