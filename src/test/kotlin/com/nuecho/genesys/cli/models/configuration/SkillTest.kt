package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSkill
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
    @Test
    fun `updateCfgObject should properly create CfgSkill`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgSkill::class.java, any()) } returns null
        mockRetrieveTenant(service)

        val cfgSkill = skill.updateCfgObject(service)

        with(cfgSkill) {
            assertEquals(skill.name, name)
            assertEquals(ConfigurationObjects.toCfgObjectState(skill.state), state)
            assertEquals(skill.userProperties, userProperties.asCategorizedProperties())
        }
    }
}

private fun mockCfgSkill() = mockCfgSkill(skill.name).apply {
    every { state } returns toCfgObjectState(skill.state)
    every { userProperties } returns mockKeyValueCollection()
}
