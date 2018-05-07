package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgRouteType
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDN
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDNGroup
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgObjectiveTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgDNRegisterFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgDNType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgRouteType
import com.nuecho.genesys.cli.models.configuration.reference.DNGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveDNGroup
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveObjectiveTable
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveSwitch
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every

private const val NUMBER = "123"
private const val SWITCH_NAME = "aswitch"
private val dn = DN(
    tenant = DEFAULT_TENANT_REFERENCE,
    number = NUMBER,
    switch = SwitchReference(SWITCH_NAME),
    type = CfgDNType.CFGNoDN.toShortName(),
    group = DNGroupReference("dnGroup"),
    association = "anassociation",
    routing = Routing(
        CfgRouteType.CFGDirect.toShortName(),
        listOf(DNReference(number = "1234", switch = "aswitch", type = CfgDNType.CFGACDQueue))
    ),
    dnLoginID = "anId",
    trunks = 1,
    override = "anoverride",
    name = "aname",
    useOverride = CfgFlag.CFGTrue.asBoolean(),
    site = FolderReference("abc"),
    contract = ObjectiveTableReference("acontract"),
    userProperties = ConfigurationTestData.defaultProperties(),
    state = CfgObjectState.CFGEnabled.toShortName()
)

class DNTest : ConfigurationObjectTest(
    dn, DN(
        tenant = DEFAULT_TENANT_REFERENCE,
        number = "123",
        switch = SwitchReference("aswitch"),
        type = CfgDNType.CFGNoDN.toShortName()
    ), DN(mockCfgDN())
) {
    init {

        "DN.updateCfgObject should properly create CfgDN" {
            val service = mockConfService()
            every { service.retrieveObject(CfgDN::class.java, any()) } returns null
            mockRetrieveTenant(service)
            mockRetrieveDNGroup(service)
            mockRetrieveSwitch(service)
            mockRetrieveFolder(service)
            mockRetrieveObjectiveTable(service)

            val (status, cfgObject) = dn.updateCfgObject(service)
            val cfgDN = cfgObject as CfgDN

            status shouldBe CREATED

            with(cfgDN) {
                name shouldBe dn.name
                switchDBID shouldBe DEFAULT_OBJECT_DBID
                registerAll shouldBe ConfigurationObjects.toCfgDNRegisterFlag(dn.registerAll)
                switchSpecificType shouldBe dn.switchSpecificType
                state shouldBe ConfigurationObjects.toCfgObjectState(dn.state)
                userProperties.asCategorizedProperties() shouldBe dn.userProperties
            }
        }
    }
}

@Suppress("LongMethod")
private fun mockCfgDN(): CfgDN {
    val cfgState = toCfgObjectState(dn.state)
    val cfgDNGroup = mockCfgDNGroup(dn.group!!.primaryKey)
    val cfgSwitch = mockCfgSwitch(dn.switch.primaryKey)
    val cfgSite = mockCfgFolder(dn.site!!.primaryKey)
    val cfgDestDN = mockCfgDN(dn.routing!!.destinationDNs.first().number).apply {
        every { switch } returns cfgSwitch
        every { type } returns CfgDNType.CFGACDQueue
        every { name } returns null
    }
    val cfgObjectiveTable = mockCfgObjectiveTable(dn.contract!!.primaryKey)
    every { cfgObjectiveTable.dbid } returns 111

    return mockCfgDN(dn.number).apply {
        every { group } returns cfgDNGroup
        every { switch } returns cfgSwitch
        every { registerAll } returns toCfgDNRegisterFlag(dn.registerAll)
        every { switchSpecificType } returns dn.switchSpecificType

        every { type } returns toCfgDNType(dn.type)
        every { association } returns dn.association

        every { routeType } returns toCfgRouteType(dn.routing?.type)
        every { destDNs } returns listOf(cfgDestDN)
        every { dnLoginID } returns dn.dnLoginID
        every { trunks } returns dn.trunks
        every { override } returns dn.override
        every { accessNumbers } returns emptyList()
        every { state } returns cfgState
        every { userProperties } returns mockKeyValueCollection()

        every { name } returns dn.name
        every { useOverride } returns ConfigurationObjects.toCfgFlag(dn.useOverride)
        every { site } returns cfgSite
        every { contract } returns cfgObjectiveTable
    }
}
