package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgAppRank
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.commons.collections.KeyValueCollection
import com.genesyslab.platform.commons.collections.KeyValuePair
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgRank
import com.nuecho.genesys.cli.TestResources
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgAgentLoginInfo
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSkillLevel
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.staticMockk
import io.mockk.use

class PersonTest : StringSpec() {
    private val service = ConfService(Environment(host = "test", user = "test", rawPassword = "test"))
    private val mapper = jacksonObjectMapper()

    private val person = Person(
        employeeId = "employeeId",
        externalId = "externalId",
        firstName = "firstName",
        lastName = "lastName",
        userName = "userName",
        password = "password",
        passwordHashAlgorithm = 1,
        passwordUpdatingDate = 20180314,
        changePasswordOnNextLogin = false,
        emailAddress = "emailAddress",
        state = CfgObjectState.CFGEnabled.toShortName(),
        agent = true,
        externalAuth = false,
        appRanks = mapOf(
            CfgAppType.CFGAdvisors.toShortName() to CfgRank.CFGUser.toShortName(),
            CfgAppType.CFGAgentDesktop.toShortName() to CfgRank.CFGDesigner.toShortName()
        ),
        userProperties = mapOf(
            "number" to 123,
            "string" to "abc",
            "bytes" to "abc".toByteArray(),
            "subProperties" to mapOf(
                "subNumber" to 456,
                "subString" to "def",
                "subBytes" to "def".toByteArray()
            )
        ),
        agentInfo = AgentInfo(
            capacityRule = "capacityRule",
            contract = "contract",
            place = "place",
            site = "site",
            skillLevels = mapOf(
                "skill_1" to 10,
                "skill_2" to 20,
                "skill_3" to 30
            ),
            agentLogins = listOf(
                AgentLoginInfo("agent_1", 1000),
                AgentLoginInfo("agent_2", 2000),
                AgentLoginInfo("agent_3", 3000)
            )
        )
    )

    init {
        "empty Person should properly serialize" {
            checkSerialization(Person("employedId", "userName"), "empty_person")
        }

        "fully initialized Person should properly serialize" {
            checkSerialization(person, "person")
        }

        "CfgPerson initialized Person should properly serialize" {
            val person = Person(mockCfgPerson())
            checkSerialization(person, "person")
        }

        "CfgPerson.import should properly update CfgPerson" {
            val importedPerson = CfgPerson(service)
            val dbid = 101

            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
                ConfServiceExtensionMocks.mockRetrieveAgentLogin(service, dbid)
                ConfServiceExtensionMocks.mockRetrieveFolder(service, dbid)
                ConfServiceExtensionMocks.mockRetrieveObjectiveTable(service, dbid)
                ConfServiceExtensionMocks.mockRetrievePlace(service, dbid)
                ConfServiceExtensionMocks.mockRetrieveScript(service, dbid)
                ConfServiceExtensionMocks.mockRetrieveSkill(service, dbid)
                importedPerson.import(person)
            }

            importedPerson.employeeID shouldBe person.employeeId
            importedPerson.externalID shouldBe person.externalId
            importedPerson.firstName shouldBe person.firstName
            importedPerson.lastName shouldBe person.lastName
            importedPerson.userName shouldBe person.userName
            importedPerson.password shouldBe person.password
            importedPerson.passwordHashAlgorithm shouldBe person.passwordHashAlgorithm
            importedPerson.passwordUpdatingDate shouldBe person.passwordUpdatingDate
            importedPerson.changePasswordOnNextLogin shouldBe toCfgFlag(person.changePasswordOnNextLogin)
            importedPerson.emailAddress shouldBe person.emailAddress
            importedPerson.state shouldBe toCfgObjectState(person.state)
            importedPerson.isAgent shouldBe toCfgFlag(person.agent)
            importedPerson.isExternalAuth shouldBe toCfgFlag(person.externalAuth)
            importedPerson.appRanks.size shouldBe 2
            importedPerson.userProperties.size shouldBe 4

            val agentInfo = importedPerson.agentInfo
            agentInfo.siteDBID shouldBe dbid
            agentInfo.placeDBID shouldBe dbid
            agentInfo.contractDBID shouldBe dbid
            agentInfo.capacityRuleDBID shouldBe dbid
            agentInfo.skillLevels.size shouldBe 3
            agentInfo.agentLogins.size shouldBe 0
        }
    }

    private fun mockCfgPerson(): CfgPerson {
        val appRanks = listOf(
            mockCfgAppRank(CfgAppType.CFGAdvisors, CfgRank.CFGUser),
            mockCfgAppRank(CfgAppType.CFGAgentDesktop, CfgRank.CFGDesigner)
        )

        val agentInfo = mockCfgAgentInfo()

        val cfgPerson = mockk<CfgPerson>()
        every { cfgPerson.employeeID } returns person.employeeId
        every { cfgPerson.externalID } returns person.externalId
        every { cfgPerson.firstName } returns person.firstName
        every { cfgPerson.lastName } returns person.lastName
        every { cfgPerson.userName } returns person.userName
        every { cfgPerson.password } returns person.password
        every { cfgPerson.passwordHashAlgorithm } returns person.passwordHashAlgorithm
        every { cfgPerson.passwordUpdatingDate } returns person.passwordUpdatingDate
        every { cfgPerson.changePasswordOnNextLogin } returns CfgFlag.CFGFalse
        every { cfgPerson.emailAddress } returns person.emailAddress
        every { cfgPerson.state } returns CfgObjectState.CFGEnabled
        every { cfgPerson.isAgent } returns CfgFlag.CFGTrue
        every { cfgPerson.isExternalAuth } returns CfgFlag.CFGFalse
        every { cfgPerson.appRanks } returns appRanks
        every { cfgPerson.userProperties } returns mockKeyValueCollection()
        every { cfgPerson.agentInfo } returns agentInfo
        return cfgPerson
    }

    private fun mockCfgAppRank(appType: CfgAppType, appRank: CfgRank): CfgAppRank {
        val cfgAppRank = mockk<CfgAppRank>()
        every { cfgAppRank.appType } returns appType
        every { cfgAppRank.appRank } returns appRank
        return cfgAppRank
    }

    private fun mockKeyValueCollection(): KeyValueCollection {
        val subKeyValueCollection = KeyValueCollection()
        subKeyValueCollection.addPair(KeyValuePair("subNumber", 456))
        subKeyValueCollection.addPair(KeyValuePair("subString", "def"))
        subKeyValueCollection.addPair(KeyValuePair("subBytes", "def".toByteArray()))

        val keyValueCollection = KeyValueCollection()
        keyValueCollection.addPair(KeyValuePair("number", 123))
        keyValueCollection.addPair(KeyValuePair("string", "abc"))
        keyValueCollection.addPair(KeyValuePair("bytes", "abc".toByteArray()))
        keyValueCollection.addPair(KeyValuePair("subProperties", subKeyValueCollection))

        return keyValueCollection
    }

    private fun mockCfgAgentInfo(): CfgAgentInfo {
        val capacityRule = mockk<CfgScript>()
        every { capacityRule.name } returns "capacityRule"

        val contract = mockk<CfgObjectiveTable>()
        every { contract.name } returns "contract"

        val place = mockk<CfgPlace>()
        every { place.name } returns "place"

        val site = mockk<CfgFolder>()
        every { site.name } returns "site"

        val skillLevels = listOf(
            mockCfgSkillLevel("skill_1", 10),
            mockCfgSkillLevel("skill_2", 20),
            mockCfgSkillLevel("skill_3", 30)
        )

        val agentLogins = listOf(
            mockCfgAgentLoginInfo("agent_1", 1000),
            mockCfgAgentLoginInfo("agent_2", 2000),
            mockCfgAgentLoginInfo("agent_3", 3000)
        )

        val agentInfo = mockk<CfgAgentInfo>()
        every { agentInfo.capacityRule } returns capacityRule
        every { agentInfo.contract } returns contract
        every { agentInfo.place } returns place
        every { agentInfo.site } returns site
        every { agentInfo.skillLevels } returns skillLevels
        every { agentInfo.agentLogins } returns agentLogins
        return agentInfo
    }

    private fun checkSerialization(configurationObject: ConfigurationObject, expectedFile: String) {
        val stringResult = mapper.writeValueAsString(configurationObject)
        val jsonResult = mapper.readTree(stringResult)
        jsonResult shouldBe TestResources.loadRawConfiguration("models/configuration/$expectedFile.json")
    }
}
