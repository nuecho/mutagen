package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSkill
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every

private val skill = Skill(
    name = "foo",
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = defaultProperties()
)

class SkillTest : ConfigurationObjectTest(
    configurationObject = skill,
    emptyConfigurationObject = Skill("foo"),
    importedConfigurationObject = Skill(mockCfgSkill())
) {
    init {
        val service = mockConfService()

        "Skill.updateCfgObject should properly create CfgSkill" {
            every { service.retrieveObject(CfgSkill::class.java, any()) } returns null

            val (status, cfgObject) = skill.updateCfgObject(service)
            val cfgSkill = cfgObject as CfgSkill

            status shouldBe CREATED

            with(cfgSkill) {
                name shouldBe skill.name
                state shouldBe ConfigurationObjects.toCfgObjectState(skill.state)
                userProperties.asCategorizedProperties() shouldBe skill.userProperties
            }
        }
    }
}

private fun mockCfgSkill() = mockCfgSkill(skill.name).also {
    every { it.state } returns toCfgObjectState(skill.state)
    every { it.userProperties } returns mockKeyValueCollection()
}
