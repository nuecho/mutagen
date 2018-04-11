package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgRole
import com.genesyslab.platform.applicationblocks.com.objects.CfgRoleMember
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPerson
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkUserProperties
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.dbidToPrimaryKey
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.retrieveRole
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.objectMockk
import io.mockk.staticMockk
import io.mockk.use

private const val PERSON1 = "dmorand"
private const val PERSON2 = "fparga"
private const val PERSON3 = "pdeschen"

class RoleTest : StringSpec() {
    private val service = ConfService(Environment(host = "test", user = "test", rawPassword = "test"))

    private val role = Role(
        name = "name",
        description = "description",
        state = CfgObjectState.CFGEnabled.toShortName(),
        members = sortedSetOf("person/$PERSON3", "person/$PERSON2", "person/$PERSON1"),
        userProperties = defaultProperties()
    )

    init {
        "empty Role should properly serialize" {
            checkSerialization(Role("name"), "empty_role")
        }

        "fully initialized Role should properly serialize" {
            checkSerialization(role, "role")
        }

        "Role should properly deserialize" {
            val deserializedRole = loadJsonConfiguration(
                "models/configuration/role.json",
                Role::class.java
            )

            // Normally we should simply check that 'deserialized shouldBe role' but since Role.equals is broken
            // because of ByteArray.equals, this should do the trick for now.
            checkSerialization(deserializedRole, "role")
            checkUserProperties(role.userProperties!!, deserializedRole.userProperties!!)
        }

        "CfgRole initialized Role should properly serialize" {
            objectMockk(ConfigurationObjects).use {
                every {
                    dbidToPrimaryKey(any(), any(), any())
                } returns PERSON3 andThen PERSON2 andThen PERSON1

                val role = Role(mockCfgRole())
                checkSerialization(role, "role")
            }
        }

        "Role.updateCfgObject should properly create CfgRole" {
            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {

                every { service.retrieveRole(any()) } returns null

                val (status, cfgObject) = role.updateCfgObject(service)
                val cfgRole = cfgObject as CfgRole

                status shouldBe CREATED

                with(cfgRole) {
                    name shouldBe role.name
                    description shouldBe role.description
                    state shouldBe toCfgObjectState(role.state)
                    members.size shouldBe 0
                    userProperties.size shouldBe 4
                }
            }
        }
    }

    private fun mockCfgRole(): CfgRole {
        val cfgRole = mockk<CfgRole>()

        val member = CfgRoleMember(service, cfgRole)
        member.objectDBID = 101
        member.objectType = CFGPerson

        val members = listOf(member, member, member)
        val state = toCfgObjectState(role.state)

        every { cfgRole.name } returns role.name
        every { cfgRole.description } returns role.description
        every { cfgRole.state } returns state
        every { cfgRole.members } returns members
        every { cfgRole.userProperties } returns mockKeyValueCollection()
        every { cfgRole.configurationService } returns service

        return cfgRole
    }
}
