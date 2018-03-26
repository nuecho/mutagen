package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgAppRank
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgRank
import com.nuecho.genesys.cli.TestResources
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgAgentLoginInfo
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSkillLevel
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
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
    private val mapper = defaultJsonObjectMapper()

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
        userProperties = defaultProperties(),
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

        "Person should properly deserialize" {
            val deserializedPerson = TestResources.loadJsonConfiguration(
                "models/configuration/person.json",
                Person::class.java
            )

            // Normally we should simply check that 'deserialized shouldBe person' but since Person.equals is broken
            // because of ByteArray.equals, this should do the trick for now.
            checkSerialization(deserializedPerson, "person")

            // Ensure that byte arrays are properly deserialized (GC-60)
            val actualByteArray = deserializedPerson.userProperties!!["bytes"] as ByteArray
            val expectedByteArray = person.userProperties!!["bytes"] as ByteArray
            actualByteArray.contentEquals(expectedByteArray) shouldBe true
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

            with(importedPerson) {
                employeeID shouldBe person.employeeId
                externalID shouldBe person.externalId
                firstName shouldBe person.firstName
                lastName shouldBe person.lastName
                userName shouldBe person.userName
                password shouldBe person.password
                passwordHashAlgorithm shouldBe person.passwordHashAlgorithm
                passwordUpdatingDate shouldBe person.passwordUpdatingDate
                changePasswordOnNextLogin shouldBe toCfgFlag(person.changePasswordOnNextLogin)
                emailAddress shouldBe person.emailAddress
                state shouldBe toCfgObjectState(person.state)
                isAgent shouldBe toCfgFlag(person.agent)
                isExternalAuth shouldBe toCfgFlag(person.externalAuth)
                appRanks.size shouldBe 2
                userProperties.size shouldBe 4
            }

            with(importedPerson.agentInfo) {
                siteDBID shouldBe dbid
                placeDBID shouldBe dbid
                contractDBID shouldBe dbid
                capacityRuleDBID shouldBe dbid
                skillLevels.size shouldBe 3
                agentLogins.size shouldBe 0
            }
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
}
