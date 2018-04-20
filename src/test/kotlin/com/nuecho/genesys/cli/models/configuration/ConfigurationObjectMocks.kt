package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLoginInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkillLevel
import com.genesyslab.platform.commons.collections.KeyValueCollection
import com.genesyslab.platform.commons.collections.KeyValuePair
import io.mockk.every
import io.mockk.mockk

object ConfigurationObjectMocks {
    const val SUB_NUMBER = 456
    const val NUMBER = 123

    private fun mockCfgAgentLogin(loginCode: String): CfgAgentLogin {
        val cfgAgentLogin = mockk<CfgAgentLogin>()
        every { cfgAgentLogin.loginCode } returns loginCode
        return cfgAgentLogin
    }

    fun mockCfgAgentLoginInfo(loginCode: String, wrapupTime: Int): CfgAgentLoginInfo {
        val agentLogin = mockCfgAgentLogin(loginCode)

        val cfgAgentLoginInfo = mockk<CfgAgentLoginInfo>()
        every { cfgAgentLoginInfo.agentLogin } returns agentLogin
        every { cfgAgentLoginInfo.wrapupTime } returns wrapupTime
        return cfgAgentLoginInfo
    }

    fun mockCfgSkillLevel(skillName: String, skillLevel: Int): CfgSkillLevel {
        val cfgSkill = mockk<CfgSkill>()
        every { cfgSkill.name } returns skillName

        val cfgSkillLevel = mockk<CfgSkillLevel>()
        every { cfgSkillLevel.skill } returns cfgSkill
        every { cfgSkillLevel.level } returns skillLevel

        return cfgSkillLevel
    }

    fun mockKeyValueCollection(): KeyValueCollection {
        val subKeyValueCollection = KeyValueCollection()
        with(subKeyValueCollection) {
            addPair(KeyValuePair("subNumber", SUB_NUMBER))
            addPair(KeyValuePair("subString", "def"))
            addPair(KeyValuePair("subBytes", "def".toByteArray()))
        }

        val keyValueCollection = KeyValueCollection()
        with(keyValueCollection) {
            addPair(KeyValuePair("number", NUMBER))
            addPair(KeyValuePair("string", "abc"))
            addPair(KeyValuePair("bytes", "abc".toByteArray()))
            addPair(KeyValuePair("subProperties", subKeyValueCollection))
        }

        return keyValueCollection
    }
}
