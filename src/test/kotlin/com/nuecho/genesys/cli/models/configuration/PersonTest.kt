package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgAppRank
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgRank
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgAgentLoginInfo
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgObjectiveTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPlace
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSkillLevel
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.retrievePerson
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.staticMockk
import io.mockk.use

private const val EMPLOYEE_ID = "employeeId"
private val person = Person(
    employeeId = EMPLOYEE_ID,
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

class PersonTest : ConfigurationObjectTest(person, Person(EMPLOYEE_ID), Person(mockCfgPerson())) {
    init {
        val service = mockConfService()

        "Person.updateCfgObject should properly create CfgPerson" {
            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
                val dbid = 101

                every { service.retrievePerson(any()) } returns null
                ConfServiceExtensionMocks.mockRetrieveAgentLogin(service, dbid)
                ConfServiceExtensionMocks.mockRetrieveFolder(service, dbid)
                ConfServiceExtensionMocks.mockRetrieveObjectiveTable(service, dbid)
                ConfServiceExtensionMocks.mockRetrievePlace(service, dbid)
                ConfServiceExtensionMocks.mockRetrieveScript(service, dbid)
                ConfServiceExtensionMocks.mockRetrieveSkill(service, dbid)

                val (status, cfgObject) = person.updateCfgObject(service)
                val cfgPerson = cfgObject as CfgPerson

                status shouldBe CREATED

                with(cfgPerson) {
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

                with(cfgPerson.agentInfo) {
                    siteDBID shouldBe dbid
                    placeDBID shouldBe dbid
                    contractDBID shouldBe dbid
                    capacityRuleDBID shouldBe dbid
                    skillLevels.size shouldBe 3
                    agentLogins.size shouldBe 0
                }
            }
        }

        "Person.updateCfgObject should use employeeId when username is not specified" {
            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
                every { service.retrievePerson(any()) } returns null

                val (_, cfgObject) = Person(EMPLOYEE_ID).updateCfgObject(service)
                val cfgPerson = cfgObject as CfgPerson

                with(cfgPerson) {
                    employeeID shouldBe EMPLOYEE_ID
                    userName shouldBe EMPLOYEE_ID
                }
            }
        }
    }
}

@Suppress("LongMethod")
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
    every { cfgPerson.state } returns toCfgObjectState(person.state)
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

@Suppress("LongMethod")
private fun mockCfgAgentInfo(): CfgAgentInfo {
    val capacityRule = mockCfgScript("capacityRule")
    val contract = mockCfgObjectiveTable("contract")
    val place = mockCfgPlace("place")
    val site = mockCfgFolder("site")

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
