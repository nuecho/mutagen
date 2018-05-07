package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgRole
import com.genesyslab.platform.applicationblocks.com.objects.CfgRoleMember
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPerson
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPerson
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgRole
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use

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
    userProperties = defaultProperties()
)

class RoleTest : ConfigurationObjectTest(
    role,
    Role(tenant = DEFAULT_TENANT_REFERENCE, name = NAME)
) {
    init {
        "CfgRole initialized Role should properly serialize" {
            val service = mockConfService()
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

        "Role.updateCfgObject should properly create CfgRole" {
            val service = mockConfService()
            every { service.retrieveObject(CfgRole::class.java, any()) } returns null
            mockRetrieveTenant(service)

            val (status, cfgObject) = role.updateCfgObject(service)
            val cfgRole = cfgObject as CfgRole

            status shouldBe CREATED

            with(cfgRole) {
                name shouldBe role.name
                description shouldBe role.description
                state shouldBe toCfgObjectState(role.state)
                members.size shouldBe 0
                userProperties.asCategorizedProperties() shouldBe role.userProperties
            }
        }
    }
}

private fun mockCfgRole(service: IConfService): CfgRole {
    val cfgRole = mockCfgRole(role.name)

    val cfgState = toCfgObjectState(role.state)
    val member = CfgRoleMember(service, cfgRole)
    member.objectDBID = 101
    member.objectType = CFGPerson

    return cfgRole.apply {
        every { description } returns role.description
        every { state } returns cfgState
        every { members } returns listOf(member, member, member)
        every { userProperties } returns mockKeyValueCollection()
        every { configurationService } returns service
    }
}
