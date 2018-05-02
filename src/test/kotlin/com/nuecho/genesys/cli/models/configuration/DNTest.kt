package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgRouteType
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDN
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDNGroup
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgObjectiveTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.getSwitchDbid
import com.nuecho.genesys.cli.services.retrieveDN
import com.nuecho.genesys.cli.services.retrieveDNGroup
import com.nuecho.genesys.cli.services.retrieveDNs
import com.nuecho.genesys.cli.services.retrieveFolder
import com.nuecho.genesys.cli.services.retrieveObjectiveTable
import com.nuecho.genesys.cli.services.retrieveSwitch
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every
import io.mockk.staticMockk
import io.mockk.use

private const val SWITCH_DBID = 1
private val dn = DN(
    number = "123",
    switch = DNSwitch("aswitch"),
    group = "dnGroup",
    association = "anassociation",
    routing = Routing(CfgRouteType.CFGDirect.toShortName(), setOf("1234")),
    dnLoginID = "anId",
    trunks = 1,
    override = "anoverride",
    name = "aname",
    useOverride = CfgFlag.CFGTrue.asBoolean(),
    site = "abc",
    contract = "acontract",
    userProperties = ConfigurationTestData.defaultProperties(),
    state = CfgObjectState.CFGEnabled.toShortName()
)

class DNTest : ConfigurationObjectTest(
    dn, DN(
        number = "123",
        switch = DNSwitch(name = "aswitch")
    ), DN(mockCfgDN())
) {
    init {
        val service = mockConfService()

        "DN.updateCfgObject should properly create CfgDN" {
            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
                val cfgSwitch = mockCfgSwitch(dn.switch.name)
                every { cfgSwitch.objectDbid } returns SWITCH_DBID

                val cfgFolder = mockCfgFolder(dn.site)
                every { cfgFolder.objectDbid } returns 2

                val cfgObjectiveTable = mockCfgObjectiveTable(dn.contract)
                every { cfgObjectiveTable.objectDbid } returns 3

                val cfgDNGroup = mockCfgDNGroup(dn.group)
                every { cfgDNGroup.objectDbid } returns 5

                every { service.retrieveDN(any()) } returns null
                every { service.retrieveDNGroup(any()) } returns cfgDNGroup
                every { service.retrieveSwitch(any()) } returns cfgSwitch
                every { service.retrieveFolder(any()) } returns cfgFolder
                every { service.retrieveDNs(any()) } returns null
                every { service.retrieveObjectiveTable(any()) } returns cfgObjectiveTable

                every { service.getSwitchDbid(any()) } returns SWITCH_DBID

                val (status, cfgObject) = dn.updateCfgObject(service)
                val cfgDN = cfgObject as CfgDN

                status shouldBe CREATED

                with(cfgDN) {
                    name shouldBe dn.name
                    switchDBID shouldBe SWITCH_DBID
                    registerAll shouldBe ConfigurationObjects.toCfgDNRegisterFlag(dn.switch.registerAll)
                    switchSpecificType shouldBe dn.switch.switchSpecificType
                    state shouldBe ConfigurationObjects.toCfgObjectState(dn.state)
                    userProperties.asCategorizedProperties() shouldBe dn.userProperties
                }
            }
        }
    }
}

@Suppress("LongMethod")
private fun mockCfgDN(): CfgDN {
    val state = toCfgObjectState(dn.state)

    val cfgDNGroup = mockCfgDNGroup(dn.group)
    val cfgSwitch = mockCfgSwitch(dn.switch.name)
    val cfgSite = mockCfgFolder(dn.site)
    val cfgDestDN = mockCfgDN(dn.routing!!.destinationDNs.first())
    val cfgObjectiveTable = mockCfgObjectiveTable(dn.contract)
    every { cfgObjectiveTable.dbid } returns 111

    val cfgDN = mockCfgDN(dn.number)

    every { cfgDN.group } returns cfgDNGroup
    every { cfgDN.switch } returns cfgSwitch
    every { cfgDN.registerAll } returns ConfigurationObjects.toCfgDNRegisterFlag(dn.switch.registerAll)
    every { cfgDN.switchSpecificType } returns dn.switch.switchSpecificType

    every { cfgDN.type } returns ConfigurationObjects.toCfgDNType(dn.type)
    every { cfgDN.association } returns dn.association

    every { cfgDN.routeType } returns ConfigurationObjects.toCfgRouteType(dn.routing?.type)
    every { cfgDN.destDNs } returns listOf(cfgDestDN)
    every { cfgDN.dnLoginID } returns dn.dnLoginID
    every { cfgDN.trunks } returns dn.trunks
    every { cfgDN.override } returns dn.override
    every { cfgDN.accessNumbers } returns listOf()
    every { cfgDN.state } returns state
    every { cfgDN.userProperties } returns mockKeyValueCollection()

    every { cfgDN.name } returns dn.name
    every { cfgDN.useOverride } returns ConfigurationObjects.toCfgFlag(dn.useOverride)
    every { cfgDN.site } returns cfgSite
    every { cfgDN.contract } returns cfgObjectiveTable

    return cfgDN
}
