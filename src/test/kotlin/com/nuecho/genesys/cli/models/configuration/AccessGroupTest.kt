package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAccessGroup
import com.genesyslab.platform.configuration.protocol.types.CfgAccessGroupType
import com.genesyslab.platform.configuration.protocol.types.CfgAccessGroupType.CFGAdministratorsGroup
import com.genesyslab.platform.configuration.protocol.types.CfgAccessGroupType.CFGDefaultGroup
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPerson
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgAccessGroup
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPerson
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockEmptyCfgGroup
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.staticMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test

private const val NAME = "accessGroup"
private const val MEMBER1 = "member1"
private const val MEMBER2 = "member2"

private val accessGroup = AccessGroup(
    group = Group(
        tenant = DEFAULT_TENANT_REFERENCE,
        name = NAME,
        state = "enabled"
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
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(accessGroup.members)
            .add(accessGroup.folder)
            .add(accessGroup.group.getReferences())
            .toSet()

        assertThat(accessGroup.getReferences(), equalTo(expected))
    }

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
        val member1 = mockCfgPerson(MEMBER1)
        val member2 = mockCfgPerson(MEMBER2)

        objectMockk(ConfigurationObjects).use {
            mockRetrieveFolderByDbid(service)
            every { service.retrieveObject(CFGPerson, any()) } returns member1 andThen member2

            val accessGroup = AccessGroup(mockAccessGroup(service))
            checkSerialization(accessGroup, "accessgroup")
        }
    }

    @Test
    fun `createCfgObject should properly create CfgAccessGroup`() {
        val service = mockConfService()
        val member1Dbid = 102
        val member2Dbid = 103

        staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {

            every { service.getObjectDbid(DEFAULT_TENANT_REFERENCE) } returns DEFAULT_OBJECT_DBID
            every { service.getObjectDbid(accessGroup.members!![0]) } returns member1Dbid
            every { service.getObjectDbid(accessGroup.members!![1]) } returns member2Dbid
            every { service.retrieveObject(CfgAccessGroup::class.java, any()) } returns null

            objectMockk(ConfigurationObjectRepository).use {
                mockConfigurationObjectRepository()
                val cfgAccessGroup = accessGroup.createCfgObject(service)

                with(cfgAccessGroup) {
                    assertThat(type, equalTo(CFGDefaultGroup))
                    assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
                    assertThat(memberIDs, hasSize(2))
                    assertThat(memberIDs.toList()[0].dbid, equalTo(member1Dbid))
                    assertThat(memberIDs.toList()[1].dbid, equalTo(member2Dbid))
                    assertThat(memberIDs.toList()[0].type, equalTo(CFGPerson))
                    assertThat(memberIDs.toList()[1].type, equalTo(CFGPerson))

                    assertThat(groupInfo.name, equalTo(accessGroup.group.name))
                    assertThat(groupInfo, equalTo(accessGroup.group.toCfgGroup(service, cfgAccessGroup)))
                    assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
                }
            }
        }
    }
}

private fun mockAccessGroup(service: IConfService): CfgAccessGroup {
    val cfgAccessGroup = mockCfgAccessGroup(accessGroup.group.name)

    val membersMock = accessGroup.members?.map { mockCfgID(CFGPerson) }
    val groupInfoMock = mockEmptyCfgGroup(accessGroup.group.name)

    return cfgAccessGroup.apply {
        every { configurationService } returns service
        every { memberIDs } returns membersMock
        every { type } returns CfgAccessGroupType.CFGDefaultGroup
        every { folderId } returns DEFAULT_FOLDER_DBID
        every { groupInfo } returns groupInfoMock
    }
}
