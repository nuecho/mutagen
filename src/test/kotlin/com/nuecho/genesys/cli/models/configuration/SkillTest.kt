package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSkill
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every

private const val NAME = "name"
private val skill = Skill(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = defaultProperties()
)

class SkillTest : ConfigurationObjectTest(
    skill,
    Skill(tenant = DEFAULT_TENANT_REFERENCE, name = NAME),
    Skill(mockCfgSkill())
) {
    init {
        "Skill.updateCfgObject should properly create CfgSkill" {
            val service = mockConfService()
            every { service.retrieveObject(CfgSkill::class.java, any()) } returns null
            mockRetrieveTenant(service)

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

private fun mockCfgSkill() = mockCfgSkill(skill.name).apply {
    every { state } returns toCfgObjectState(skill.state)
    every { userProperties } returns mockKeyValueCollection()
}
