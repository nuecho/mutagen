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
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
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
    emptySet(),
    Skill(mockCfgSkill())
) {
    @Test
    fun `updateCfgObject should properly create CfgSkill`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgSkill::class.java, any()) } returns null
        mockRetrieveTenant(service)

        val cfgSkill = skill.updateCfgObject(service)

        with(cfgSkill) {
            assertThat(name, equalTo(skill.name))
            assertThat(state, equalTo(ConfigurationObjects.toCfgObjectState(skill.state)))
            assertThat(userProperties.asCategorizedProperties(), equalTo(skill.userProperties))
        }
    }
}

private fun mockCfgSkill() = mockCfgSkill(skill.name).apply {
    every { state } returns toCfgObjectState(skill.state)
    every { userProperties } returns mockKeyValueCollection()
}
