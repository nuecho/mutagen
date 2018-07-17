package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgRole
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPerson
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPerson
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgRole
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgRoleMember
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test

private const val NAME = "name"
private const val PERSON1 = "dmorand"
private const val PERSON2 = "fparga"
private const val PERSON3 = "pdeschen"
private val role = Role(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    description = "description",
    state = CfgObjectState.CFGEnabled.toShortName(),
    members = sortedSetOf("person/$PERSON3", "person/$PERSON2", "person/$PERSON1"),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class RoleTest : ConfigurationObjectTest(
    role,
    Role(tenant = DEFAULT_TENANT_REFERENCE, name = NAME),
    emptySet()
) {
    @Test
    override fun `initialized object should properly serialize`() {
        val service = mockConfService()
        mockRetrieveFolderByDbid(service)

        val person1 = mockCfgPerson(PERSON1)
        val person2 = mockCfgPerson(PERSON2)
        val person3 = mockCfgPerson(PERSON3)

        objectMockk(ConfigurationObjects).use {
            every {
                service.retrieveObject(CFGPerson, any())
            } returns person3 andThen person2 andThen person1

            val role = Role(mockCfgRole(service))
            checkSerialization(role, "role")
        }
    }

    @Test
    fun `createCfgObject should properly create CfgRole`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgRole::class.java, any()) } returns null
        mockRetrieveTenant(service)

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()

            val cfgRole = role.createCfgObject(service)

            with(cfgRole) {
                assertThat(name, equalTo(role.name))
                assertThat(description, equalTo(role.description))
                assertThat(state, equalTo(toCfgObjectState(role.state)))
                assertThat(members, hasSize(0))
                assertThat(userProperties.asCategorizedProperties(), equalTo(role.userProperties))
            }
        }
    }
}

private fun mockCfgRole(service: IConfService): CfgRole {
    val cfgRole = mockCfgRole(role.name)

    val cfgState = toCfgObjectState(role.state)
    val member = mockCfgRoleMember()

    return cfgRole.apply {
        every { configurationService } returns service
        every { description } returns role.description
        every { state } returns cfgState
        every { members } returns listOf(member, member, member)
        every { userProperties } returns mockKeyValueCollection()
        every { folderId } returns DEFAULT_OBJECT_DBID
    }
}
