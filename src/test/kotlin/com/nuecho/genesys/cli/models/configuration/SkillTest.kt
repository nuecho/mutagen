package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.nuecho.genesys.cli.TestResources
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk

class SkillTest : StringSpec() {
    private val service = ConfService(Environment(host = "test", user = "test", rawPassword = "test"))

    private val skill = Skill(
        name = "foo",
        state = CfgObjectState.CFGEnabled.toShortName(),
        userProperties = defaultProperties()
    )

    init {
        "empty Skill should properly serialize" {
            checkSerialization(Skill(name = "foo"), "empty_skill")
        }

        "fully initialized Skill should properly serialize" {
            checkSerialization(skill, "skill")
        }

        "Skill should properly deserialize" {
            val skill = TestResources.loadJsonConfiguration(
                "models/configuration/skill.json",
                Skill::class.java
            )

            checkSerialization(skill, "skill")

            val actualByteArray = skill.userProperties!!["bytes"] as ByteArray
            val expectedByteArray = skill.userProperties!!["bytes"] as ByteArray
            actualByteArray.contentEquals(expectedByteArray) shouldBe true
        }

        "CfgSkill initialized Skill should properly serialize" {
            val skill = Skill(mockCfgSkill())
            checkSerialization(skill, "skill")
        }
    }

    private fun mockCfgSkill(): CfgSkill {

        val cfgSkill = mockk<CfgSkill>()
        every { cfgSkill.name } returns skill.name
        every { cfgSkill.state } returns CfgObjectState.CFGEnabled
        every { cfgSkill.userProperties } returns mockKeyValueCollection()

        return cfgSkill
    }
}
