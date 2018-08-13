package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNAccessNumber
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGACDQueue
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGNoDN
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGRoutingQueue
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGFolder
import com.genesyslab.platform.configuration.protocol.types.CfgRouteType
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDN
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDNGroup
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgObjectiveTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgDNRegisterFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgDNType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgRouteType
import com.nuecho.genesys.cli.models.configuration.reference.DNGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveDNGroup
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveObjectiveTable
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveSwitch
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.mockk
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val NUMBER = "123"
private const val SWITCH1_NAME = "switch1"
private const val SWITCH2_NAME = "switch2"
private val dn = DN(
    tenant = DEFAULT_TENANT_REFERENCE,
    number = NUMBER,
    switch = SwitchReference(SWITCH1_NAME, DEFAULT_TENANT_REFERENCE),
    type = CFGNoDN.toShortName(),
    group = DNGroupReference("dnGroup", DEFAULT_TENANT_REFERENCE),
    accessNumbers = listOf(DNAccessNumber("321", SwitchReference(SWITCH2_NAME, DEFAULT_TENANT_REFERENCE))),
    association = "anassociation",
    routeType = CfgRouteType.CFGDirect.toShortName(),
    destinationDNs = listOf(
        DNReference(
            number = "1234",
            switch = "switch1",
            type = CFGACDQueue,
            tenant = DEFAULT_TENANT_REFERENCE
        )
    ),
    dnLoginID = "anId",
    trunks = 1,
    override = "anoverride",
    name = "aname",
    useOverride = CfgFlag.CFGTrue.asBoolean(),
    site = DEFAULT_FOLDER_REFERENCE,
    contract = ObjectiveTableReference("acontract", DEFAULT_TENANT_REFERENCE),
    userProperties = ConfigurationTestData.defaultProperties(),
    state = CfgObjectState.CFGEnabled.toShortName(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class DNTest : ConfigurationObjectTest(
    configurationObject = dn,
    emptyConfigurationObject = DN(
        tenant = DEFAULT_TENANT_REFERENCE,
        number = "123",
        switch = SwitchReference("aswitch", DEFAULT_TENANT_REFERENCE),
        type = CFGRoutingQueue.toShortName()
    ),
    mandatoryProperties = setOf(ROUTE_TYPE),
    importedConfigurationObject = DN(mockCfgDN())
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(dn.tenant)
            .add(dn.switch)
            .add(dn.group)
            .add(dn.destinationDNs)
            .add(dn.site)
            .add(dn.contract)
            .add(dn.folder)
            .add(dn.accessNumbers!!.map { it.switch })
            .toSet()

        assertThat(dn.getReferences(), equalTo(expected))
    }

    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() {
        // not implemented, since object has no unchangeable properties
    }

    @Test
    fun `createCfgObject should properly create CfgDN`() {
        val service = mockConfService()
        val switchDbid = 102
        every { service.retrieveObject(CfgDN::class.java, any()) } returns null
        mockRetrieveTenant(service)
        mockRetrieveDNGroup(service)
        mockRetrieveSwitch(service, switchDbid)
        mockRetrieveObjectiveTable(service)

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgDN = dn.createCfgObject(service)

            with(cfgDN) {
                assertThat(name, equalTo(dn.name))
                assertThat(switchDBID, equalTo(switchDbid))
                assertThat(registerAll, equalTo(toCfgDNRegisterFlag(dn.registerAll)))
                assertThat(switchSpecificType, equalTo(dn.switchSpecificType))
                assertThat(state, equalTo(toCfgObjectState(dn.state)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(dn.userProperties))
                with(accessNumbers.toList()[0]) {
                    assertThat(number, equalTo(dn.accessNumbers!![0].number))
                    assertThat(switchDBID, equalTo(switchDbid))
                }
            }
        }
    }
}

private fun mockCfgDN(): CfgDN {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    val cfgState = toCfgObjectState(dn.state)
    val cfgDNGroup = mockCfgDNGroup(dn.group!!.primaryKey)
    val cfgSwitch1 = mockCfgSwitch(SWITCH1_NAME)
    val cfgSwitch2 = mockCfgSwitch(SWITCH2_NAME)
    val cfgSite = mockCfgFolder(DEFAULT_FOLDER, CFGFolder)
    val cfgDestDN = mockCfgDN(dn.destinationDNs!!.first().number).apply {
        every { switch } returns cfgSwitch1
        every { type } returns CFGACDQueue
        every { name } returns null
    }
    val cfgObjectiveTable = mockCfgObjectiveTable(dn.contract!!.primaryKey).apply {
        every { dbid } returns 111
    }
    val cfgAccessNumber = mockk<CfgDNAccessNumber>().apply {
        every { number } returns dn.accessNumbers!![0].number
        every { switch } returns cfgSwitch2
    }

    return mockCfgDN(dn.number).apply {
        every { configurationService } returns service
        every { group } returns cfgDNGroup
        every { switch } returns cfgSwitch1
        every { registerAll } returns toCfgDNRegisterFlag(dn.registerAll)
        every { switchSpecificType } returns dn.switchSpecificType

        every { type } returns toCfgDNType(dn.type)
        every { association } returns dn.association

        every { routeType } returns toCfgRouteType(dn.routeType)
        every { destDNs } returns listOf(cfgDestDN)
        every { dnLoginID } returns dn.dnLoginID
        every { trunks } returns dn.trunks
        every { override } returns dn.override
        every { accessNumbers } returns listOf(cfgAccessNumber)
        every { state } returns cfgState
        every { userProperties } returns mockKeyValueCollection()

        every { name } returns dn.name
        every { useOverride } returns toCfgFlag(dn.useOverride)
        every { site } returns cfgSite
        every { contract } returns cfgObjectiveTable
        every { folderId } returns DEFAULT_FOLDER_DBID
    }
}
