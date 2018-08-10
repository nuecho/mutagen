package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNGroup
import com.genesyslab.platform.configuration.protocol.types.CfgDNGroupType.CFGACDQueues
import com.genesyslab.platform.configuration.protocol.types.CfgDNGroupType.CFGMaxDNGroupType
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGACDQueue
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGDN
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDN
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDNGroup
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDNInfo
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockEmptyCfgGroup
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
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
private const val DN_DBID1 = 102
private const val DN_DBID2 = 103
private const val NUMBER1 = "number1"
private const val NUMBER2 = "number2"
private val DN1 = mockCfgDN(NUMBER1, DN_DBID1)
private val DN2 = mockCfgDN(NUMBER2, DN_DBID2)
private const val NAME = "name"

private val dnGroup = DNGroup(
    group = Group(
        tenant = DEFAULT_TENANT_REFERENCE,
        name = DN_GROUP,
        state = "enabled"
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

        objectMockk(ConfigurationObjects).use {
            every {
                service.retrieveObject(CFGDN, any())
            } returns DN1 andThen DN2

            val dnGroup = DNGroup(mockCfgDNGroup(service))
            checkSerialization(dnGroup, "dngroup_with_trunks")
        }
    }

    @Test
    fun `createCfgObject should properly create CfgDNGroup`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgDNGroup::class.java, any()) } returns null

        staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
            every { service.getObjectDbid(any()) } answers { DEFAULT_OBJECT_DBID }
            every { service.retrieveObject(DNReference(NUMBER1, SWITCH, CFGACDQueue, NAME)) } returns DN1
            every { service.retrieveObject(DNReference(NUMBER2, SWITCH, CFGACDQueue, NAME)) } returns DN2

            objectMockk(ConfigurationObjectRepository).use {
                mockConfigurationObjectRepository()

                val cfgDNGroup = dnGroup.createCfgObject(service)

                with(cfgDNGroup) {
                    assertThat(type, equalTo(CFGACDQueues))
                    assertThat(dNs, hasSize(2))
                    assertThat(dNs.toList()[0].trunks, `is`(nullValue()))
                    assertThat(dNs.toList()[0].dndbid, equalTo(DN_DBID1))
                    assertThat(dNs.toList()[1].trunks, equalTo(2))
                    assertThat(dNs.toList()[1].dndbid, equalTo(DN_DBID2))

                    assertThat(groupInfo, equalTo(dnGroup.group.toCfgGroup(service, this)))
                    assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
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
        every { folderId } returns DEFAULT_FOLDER_DBID
    }
}

private fun mockCfgDNGroup(service: IConfService): CfgDNGroup {
    val dnsMock = dnGroup.dns?.map { mockCfgDNInfo() }
    val groupInfoMock = mockEmptyCfgGroup(dnGroup.group.name)

    return mockCfgDNGroup(dnGroup.group.name).apply {
        every { configurationService } returns service
        every { dNs } returns dnsMock
        every { type } returns CFGACDQueues
        every { folderId } returns DEFAULT_FOLDER_DBID
        every { groupInfo } returns groupInfoMock
    }
}
