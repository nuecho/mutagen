package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLoginInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgRole
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkillLevel
import com.genesyslab.platform.applicationblocks.com.objects.CfgStatTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction
import com.genesyslab.platform.commons.collections.KeyValueCollection
import com.genesyslab.platform.commons.collections.KeyValuePair
import io.mockk.every
import io.mockk.mockk

object ConfigurationObjectMocks {
    const val NUMBER = 456

    fun mockCfgAgentLoginInfo(loginCode: String, wrapupTime: Int): CfgAgentLoginInfo {
        val agentLogin = mockCfgAgentLogin(loginCode)
        return mockk<CfgAgentLoginInfo>().also {
            every { it.agentLogin } returns agentLogin
            every { it.wrapupTime } returns wrapupTime
        }
    }

    fun mockCfgSkillLevel(skillName: String, skillLevel: Int): CfgSkillLevel {
        val cfgSkill = mockCfgSkill(skillName)

        return mockk<CfgSkillLevel>().also {
            every { it.skill } returns cfgSkill
            every { it.level } returns skillLevel
        }
    }

    fun mockKeyValueCollection(): KeyValueCollection {
        val sectionKeyValueCollection = KeyValueCollection()
        with(sectionKeyValueCollection) {
            addPair(KeyValuePair("number", NUMBER))
            addPair(KeyValuePair("string", "def"))
        }

        val keyValueCollection = KeyValueCollection()
        with(keyValueCollection) {
            addPair(KeyValuePair("section", sectionKeyValueCollection))
        }

        return keyValueCollection
    }

    fun mockCfgActionCode(name: String?) = mockk<CfgActionCode>().also { every { it.name } returns name }
    fun mockCfgAgentLogin(loginCode: String) = mockk<CfgAgentLogin>().also { every { it.loginCode } returns loginCode }
    fun mockCfgApplication(name: String) = mockk<CfgApplication>().also { every { it.name } returns name }
    fun mockCfgDN(number: String?) = mockk<CfgDN>().also { every { it.number } returns number }
    fun mockCfgDNGroup(name: String?) = mockk<CfgDNGroup>().also { every { it.groupInfo.name } returns name }
    fun mockCfgEnumerator(name: String?) = mockk<CfgEnumerator>().also { every { it.name } returns name }
    fun mockCfgFolder(name: String?) = mockk<CfgFolder>().also { every { it.name } returns name }
    fun mockCfgObjectiveTable(name: String?) = mockk<CfgObjectiveTable>().also { every { it.name } returns name }
    fun mockCfgPerson(employeeID: String?) = mockk<CfgPerson>().also { every { it.employeeID } returns employeeID }
    fun mockCfgPhysicalSwitch(name: String?) = mockk<CfgPhysicalSwitch>().also { every { it.name } returns name }
    fun mockCfgPlace(name: String?) = mockk<CfgPlace>().also { every { it.name } returns name }
    fun mockCfgRole(name: String?) = mockk<CfgRole>().also { every { it.name } returns name }
    fun mockCfgScript(name: String?) = mockk<CfgScript>().also { every { it.name } returns name }
    fun mockCfgSkill(name: String?) = mockk<CfgSkill>().also { every { it.name } returns name }
    fun mockCfgSwitch(name: String?) = mockk<CfgSwitch>().also { every { it.name } returns name }
    fun mockCfgStatTable(name: String?) = mockk<CfgStatTable>().also { every { it.name } returns name }
    fun mockCfgTenant(name: String?) = mockk<CfgTenant>().also { every { it.name } returns name }
    fun mockCfgTransaction(name: String?) = mockk<CfgTransaction>().also { every { it.name } returns name }
}
