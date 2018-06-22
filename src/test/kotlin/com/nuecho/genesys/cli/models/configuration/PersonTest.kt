package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgAppRank
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgRank
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgAgentLoginInfo
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgObjectiveTable
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPerson
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPlace
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSkillLevel
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.AgentLoginReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.PlaceReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.SkillReference
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveAgentLogin
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveObjectiveTable
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrievePlace
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveScript
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveSkill
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val EMPLOYEE_ID = "employeeId"
private val SWITCH_REFERENCE = SwitchReference("switch", DEFAULT_TENANT_REFERENCE)
private val person = Person(
    tenant = DEFAULT_TENANT_REFERENCE,
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
        capacityRule = ScriptReference("capacityRule", DEFAULT_TENANT_REFERENCE),
        contract = ObjectiveTableReference("contract", DEFAULT_TENANT_REFERENCE),
        place = PlaceReference("place", DEFAULT_TENANT_REFERENCE),
        site = FolderReference("site"),
        skillLevels = mapOf(
            SkillReference("skill_1", DEFAULT_TENANT_REFERENCE) to 10,
            SkillReference("skill_2", DEFAULT_TENANT_REFERENCE) to 20,
            SkillReference("skill_3", DEFAULT_TENANT_REFERENCE) to 30
        ),
        agentLogins = listOf(
            AgentLoginInfo(AgentLoginReference("agent_1", SWITCH_REFERENCE), 1000),
            AgentLoginInfo(AgentLoginReference("agent_2", SWITCH_REFERENCE), 2000),
            AgentLoginInfo(AgentLoginReference("agent_3", SWITCH_REFERENCE), 3000)
        )
    )
)

class PersonTest : ConfigurationObjectTest(
    person,
    Person(tenant = DEFAULT_TENANT_REFERENCE, employeeId = EMPLOYEE_ID),
    Person(mockCfgPerson())
) {
    @Test
    fun `updateCfgObject should properly create CfgPerson`() {
        val service = mockConfService()

        every { service.retrieveObject(CfgPerson::class.java, any()) } returns null
        mockRetrieveTenant(service)
        mockRetrieveAgentLogin(service)
        mockRetrieveFolder(service)
        mockRetrieveObjectiveTable(service)
        mockRetrievePlace(service)
        mockRetrieveScript(service)
        mockRetrieveSkill(service)

        val cfgPerson = person.updateCfgObject(service)

        with(cfgPerson) {
            assertEquals(person.employeeId, employeeID)
            assertEquals(person.externalId, externalID)
            assertEquals(person.firstName, firstName)
            assertEquals(person.lastName, lastName)
            assertEquals(person.userName, userName)
            assertEquals(person.password, password)
            assertEquals(person.passwordHashAlgorithm, passwordHashAlgorithm)
            assertEquals(person.passwordUpdatingDate, passwordUpdatingDate)
            assertEquals(toCfgFlag(person.changePasswordOnNextLogin), changePasswordOnNextLogin)
            assertEquals(person.emailAddress, emailAddress)
            assertEquals(toCfgObjectState(person.state), state)
            assertEquals(toCfgFlag(person.agent), isAgent)
            assertEquals(toCfgFlag(person.externalAuth), isExternalAuth)
            assertEquals(2, appRanks.size)
            assertEquals(person.userProperties, userProperties.asCategorizedProperties())
        }

        with(cfgPerson.agentInfo) {
            assertEquals(DEFAULT_OBJECT_DBID, siteDBID)
            assertEquals(DEFAULT_OBJECT_DBID, placeDBID)
            assertEquals(DEFAULT_OBJECT_DBID, contractDBID)
            assertEquals(DEFAULT_OBJECT_DBID, capacityRuleDBID)
            assertEquals(3, skillLevels.size)
            assertEquals(0, agentLogins.size)
        }
    }

    @Test
    fun `updateCfgObject should use employeeId when username is not specified`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgPerson::class.java, any()) } returns null
        mockRetrieveTenant(service)

        val cfgPerson = Person(DEFAULT_TENANT_REFERENCE, EMPLOYEE_ID).updateCfgObject(service)

        with(cfgPerson) {
            assertEquals(EMPLOYEE_ID, employeeID)
        }
    }
}

@Suppress("LongMethod")
private fun mockCfgPerson(): CfgPerson {
    val cfgAppRanks = listOf(
        mockCfgAppRank(CfgAppType.CFGAdvisors, CfgRank.CFGUser),
        mockCfgAppRank(CfgAppType.CFGAgentDesktop, CfgRank.CFGDesigner)
    )

    val cfgAgentInfo = mockCfgAgentInfo()

    return mockCfgPerson(person.employeeId).apply {
        every { externalID } returns person.externalId
        every { firstName } returns person.firstName
        every { lastName } returns person.lastName
        every { userName } returns person.userName
        every { password } returns person.password
        every { passwordHashAlgorithm } returns person.passwordHashAlgorithm
        every { passwordUpdatingDate } returns person.passwordUpdatingDate
        every { changePasswordOnNextLogin } returns CfgFlag.CFGFalse
        every { emailAddress } returns person.emailAddress
        every { state } returns toCfgObjectState(person.state)
        every { isAgent } returns CfgFlag.CFGTrue
        every { isExternalAuth } returns CfgFlag.CFGFalse
        every { appRanks } returns cfgAppRanks
        every { userProperties } returns mockKeyValueCollection()
        every { agentInfo } returns cfgAgentInfo
    }
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
