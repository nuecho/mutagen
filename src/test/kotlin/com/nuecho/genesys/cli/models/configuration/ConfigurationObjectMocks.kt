package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLoginInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkillLevel
import io.mockk.every
import io.mockk.mockk

object ConfigurationObjectMocks {
    fun mockCfgSkillLevel(skillName: String, skillLevel: Int): CfgSkillLevel {
        val cfgSkill = mockk<CfgSkill>()
        every { cfgSkill.name } returns skillName

        val cfgSkillLevel = mockk<CfgSkillLevel>()
        every { cfgSkillLevel.skill } returns cfgSkill
        every { cfgSkillLevel.level } returns skillLevel

        return cfgSkillLevel
    }

    fun mockCfgAgentLoginInfo(loginCode: String, wrapupTime: Int): CfgAgentLoginInfo {
        val agentLogin = mockCfgAgentLogin(loginCode)

        val cfgAgentLoginInfo = mockk<CfgAgentLoginInfo>()
        every { cfgAgentLoginInfo.agentLogin } returns agentLogin
        every { cfgAgentLoginInfo.wrapupTime } returns wrapupTime
        return cfgAgentLoginInfo
    }

    fun mockCfgAgentLogin(loginCode: String): CfgAgentLogin {
        val cfgAgentLogin = mockk<CfgAgentLogin>()
        every { cfgAgentLogin.loginCode } returns loginCode
        return cfgAgentLogin
    }
}
