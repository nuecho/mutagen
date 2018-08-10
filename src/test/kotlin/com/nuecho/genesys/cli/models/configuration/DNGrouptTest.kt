package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNGroup
import com.genesyslab.platform.configuration.protocol.types.CfgDNGroupType.CFGACDQueues
import com.genesyslab.platform.configuration.protocol.types.CfgDNGroupType.CFGMaxDNGroupType
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGACDQueue
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGCP
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGDN
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGFolder
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDN
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDNGroup
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDNInfo
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgObjectiveTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPerson
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgStatTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.StatTableReference
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject
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

private const val DN_GROUP = "dnGroup"
private const val SWITCH = "switch"
private const val OTHER_DBID = 102
private const val NUMBER1 = "number1"
private const val NUMBER2 = "number2"
private const val NAME = "name"

private val dnGroup = DNGroup(
    group = Group(
        tenant = DEFAULT_TENANT_REFERENCE,
        name = DN_GROUP,
        capacityTable = StatTableReference("capacityTable", DEFAULT_TENANT_REFERENCE),
        quotaTable = StatTableReference("quotaTable", DEFAULT_TENANT_REFERENCE),
        state = CfgObjectState.CFGEnabled.toShortName(),
        userProperties = ConfigurationTestData.defaultProperties(),
        capacityRule = ScriptReference("capacityRule", DEFAULT_TENANT_REFERENCE),
        site = DEFAULT_FOLDER_REFERENCE,
        contract = ObjectiveTableReference("contract", DEFAULT_TENANT_REFERENCE)
    ),
    dns = listOf(
        DNInfo(dn = DNReference(NUMBER1, SWITCH, CFGACDQueue, NAME)),
        DNInfo(dn = DNReference(NUMBER2, SWITCH, CFGACDQueue, NAME), trunks = 2)
    ),
    type = CFGACDQueues.toShortName(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class DNGrouptTest : ConfigurationObjectTest(
    configurationObject = dnGroup,
    emptyConfigurationObject = DNGroup(tenant = DEFAULT_TENANT_REFERENCE, name = DN_GROUP),
    mandatoryProperties = setOf(TYPE)
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(dnGroup.group.getReferences())
            .add(dnGroup.dns!!.map { it.dn })
            .add(dnGroup.folder)
            .toSet()

        assertThat(dnGroup.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() {
        val cfgDNGroup = mockCfgDNGroup(name = dnGroup.group.name).also {
            every { it.type } returns CFGMaxDNGroupType
        }

        assertThat(configurationObject.checkUnchangeableProperties(cfgDNGroup), equalTo(setOf(TYPE)))
    }

    @Test
    override fun `initialized object should properly serialize`() {
        val service = mockConfService()
        mockRetrieveFolderByDbid(service)

        val dn1 = mockCfgDN(NUMBER1, DEFAULT_OBJECT_DBID)
        val dn2 = mockCfgDN(NUMBER2, OTHER_DBID)

        objectMockk(ConfigurationObjects).use {
            every {
                service.retrieveObject(CFGDN, any())
            } returns dn1 andThen dn2

            val dnGroup = DNGroup(mockCfgDNGroup(service))
            checkSerialization(dnGroup, "dngroup_with_trunks")
        }
    }

    @Test
    fun `createCfgObject should properly create CfgDNGroup`() {
        val dn1 = mockCfgDN(NUMBER1, DEFAULT_OBJECT_DBID)
        val dn2 = mockCfgDN(NUMBER2, OTHER_DBID)

        val service = mockConfService()
        every { service.retrieveObject(CfgDNGroup::class.java, any()) } returns null

        staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
            every { service.getObjectDbid(any()) } answers { DEFAULT_OBJECT_DBID }
            every { service.retrieveObject(DNReference(NUMBER1, SWITCH, CFGACDQueue, NAME)) } returns dn1
            every { service.retrieveObject(DNReference(NUMBER2, SWITCH, CFGACDQueue, NAME)) } returns dn2

            objectMockk(ConfigurationObjectRepository).use {
                mockConfigurationObjectRepository()

                val cfgDNGroup = dnGroup.createCfgObject(service)

                with(cfgDNGroup) {
                    assertThat(type, equalTo(CFGACDQueues))
                    assertThat(dNs, hasSize(2))
                    assertThat(dNs.toList()[0].trunks, `is`(nullValue()))
                    assertThat(dNs.toList()[0].dndbid, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(dNs.toList()[1].trunks, equalTo(2))
                    assertThat(dNs.toList()[1].dndbid, equalTo(OTHER_DBID))

                    assertThat(groupInfo.name, equalTo(dnGroup.group.name))
                    assertThat(groupInfo.managerDBIDs, `is`(nullValue()))
                    assertThat(groupInfo.routeDNDBIDs, `is`(nullValue()))
                    assertThat(groupInfo.capacityTableDBID, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(groupInfo.quotaTableDBID, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(groupInfo.state, equalTo(toCfgObjectState(dnGroup.group.state)))
                    assertThat(groupInfo.userProperties.asCategorizedProperties(), equalTo(dnGroup.userProperties))
                    assertThat(groupInfo.capacityRuleDBID, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(groupInfo.siteDBID, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(groupInfo.contractDBID, equalTo(DEFAULT_OBJECT_DBID))
                }
            }
        }
    }
}

private fun mockCfgDN(number: String, dnDbid: Int): CfgDN {
    val cfgDN = mockCfgDN(number)

    return cfgDN.apply {
        every { getReference() } returns DNReference(number, SWITCH, CFGACDQueue, NAME)
        every { dbid } returns dnDbid
        every { name } returns NAME
        every { switch.getReference() } returns SwitchReference(SWITCH, DEFAULT_TENANT_REFERENCE)
        every { switch.name } returns SWITCH
        every { switch.tenant.getReference() } returns DEFAULT_TENANT_REFERENCE
        every { switch.tenant.name } returns NAME
        every { tenant.name } returns "tenant"
        every { trunks } returns 0
        every { type.toShortName() } returns CFGACDQueue.toShortName()
        every { folderId } returns DEFAULT_OBJECT_DBID
    }
}

private fun mockCfgDNGroup(service: IConfService): CfgDNGroup {
    val cfgDNGroup = mockCfgDNGroup(dnGroup.group.name)

    val dnsMock = dnGroup.dns?.map { mockCfgDNInfo() }
    val managersMock = dnGroup.group.managers?.map { ref -> mockCfgPerson(ref.primaryKey) }
    val cfgSwitch = mockCfgSwitch(SWITCH)
    val routeDNsMock = dnGroup.group.routeDNs?.map { ref ->
        mockCfgDN(ref.number).apply {
            every { switch } returns cfgSwitch
            every { type } returns CFGCP
            every { name } returns null
        }
    }
    val capacityTableMock = mockCfgStatTable(dnGroup.group.capacityTable!!.primaryKey)
    val quotaTableMock = mockCfgStatTable(dnGroup.group.quotaTable!!.primaryKey)
    val capacityRuleMock = mockCfgScript(dnGroup.group.capacityRule!!.primaryKey)
    val siteMock = mockCfgFolder(DEFAULT_FOLDER, CFGFolder)
    val contractMock = mockCfgObjectiveTable(dnGroup.group.contract!!.primaryKey)

    return cfgDNGroup.apply {
        every { configurationService } returns service
        every { dNs } returns dnsMock
        every { type } returns CFGACDQueues
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
