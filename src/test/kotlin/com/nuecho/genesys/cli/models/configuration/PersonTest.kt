/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgAppRank
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.genesyslab.platform.configuration.protocol.types.CfgAppType.CFGAdvisors
import com.genesyslab.platform.configuration.protocol.types.CfgAppType.CFGAgentDesktop
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGFolder
import com.genesyslab.platform.configuration.protocol.types.CfgRank
import com.genesyslab.platform.configuration.protocol.types.CfgRank.CFGDesigner
import com.genesyslab.platform.configuration.protocol.types.CfgRank.CFGUser
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
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
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.PlaceReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.SkillReference
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveAgentLogin
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveObjectiveTable
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrievePlace
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveScript
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveSkill
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.mockk
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
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
    changePasswordOnNextLogin = false,
    emailAddress = "emailAddress",
    state = CfgObjectState.CFGEnabled.toShortName(),
    agent = true,
    externalAuth = false,
    appRanks = mapOf(
        CFGAdvisors.toShortName() to CFGUser.toShortName(),
        CFGAgentDesktop.toShortName() to CFGDesigner.toShortName()
    ),
    userProperties = defaultProperties(),
    agentInfo = AgentInfo(
        capacityRule = ScriptReference("capacityRule", DEFAULT_TENANT_REFERENCE),
        contract = ObjectiveTableReference("contract", DEFAULT_TENANT_REFERENCE),
        place = PlaceReference("place", DEFAULT_TENANT_REFERENCE),
        site = DEFAULT_FOLDER_REFERENCE,
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
    ),
    folder = DEFAULT_FOLDER_REFERENCE
)

class PersonTest : ConfigurationObjectTest(
    configurationObject = person,
    emptyConfigurationObject = Person(tenant = DEFAULT_TENANT_REFERENCE, employeeId = EMPLOYEE_ID),
    mandatoryProperties = setOf(USER_NAME),
    importedConfigurationObject = Person(mockCfgPerson())
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(person.tenant)
            .add(person.agentInfo!!.capacityRule)
            .add(person.agentInfo!!.contract)
            .add(person.agentInfo!!.place)
            .add(person.agentInfo!!.site)
            .add(person.agentInfo!!.skillLevels!!.keys)
            .add(person.agentInfo!!.agentLogins!!.map { it.agentLogin })
            .add(person.folder)
            .toSet()

        assertThat(person.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        assertUnchangeableProperties(mockCfgPerson(), FOLDER)

    @Test
    fun `createCfgObject should properly create CfgPerson`() {
        val service = mockConfService()
        val placeDbid = 102
        val objectiveTableDbid = 103
        val scriptDbid = 104
        val skillDbid = 105

        every { service.retrieveObject(CfgPerson::class.java, any()) } returns null
        mockRetrieveTenant(service)
        mockRetrieveAgentLogin(service)
        mockRetrieveObjectiveTable(service, objectiveTableDbid)
        mockRetrievePlace(service, placeDbid)
        mockRetrieveScript(service, scriptDbid)
        mockRetrieveSkill(service, skillDbid)

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()

            val cfgPerson = person.createCfgObject(service)

            with(cfgPerson) {
                assertThat(employeeID, equalTo(person.employeeId))
                assertThat(externalID, equalTo(person.externalId))
                assertThat(firstName, equalTo(person.firstName))
                assertThat(lastName, equalTo(person.lastName))
                assertThat(userName, equalTo(person.userName))
                assertThat(password, equalTo(person.password))
                assertThat(passwordHashAlgorithm, equalTo(person.passwordHashAlgorithm))
                assertThat(changePasswordOnNextLogin, equalTo(toCfgFlag(person.changePasswordOnNextLogin)))
                assertThat(emailAddress, equalTo(person.emailAddress))
                assertThat(state, equalTo(toCfgObjectState(person.state)))
                assertThat(isAgent, equalTo(toCfgFlag(person.agent)))
                assertThat(isExternalAuth, equalTo(toCfgFlag(person.externalAuth)))
                assertThat(appRanks, hasSize(2))
                assertThat(userProperties.asCategorizedProperties(), equalTo(person.userProperties))
            }

            with(cfgPerson.agentInfo) {
                assertThat(siteDBID, equalTo(DEFAULT_FOLDER_DBID))
                assertThat(placeDBID, equalTo(placeDbid))
                assertThat(contractDBID, equalTo(objectiveTableDbid))
                assertThat(capacityRuleDBID, equalTo(scriptDbid))
                assertThat(skillLevels, hasSize(3))
                assertThat(skillLevels.toList()[0].skillDBID, equalTo(skillDbid))
                assertThat(agentLogins, hasSize(0))
            }
        }
    }

    @Test
    fun `createCfgObject should use employeeId when username is not specified`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgPerson::class.java, any()) } returns null
        mockRetrieveTenant(service)

        val cfgPerson = Person(DEFAULT_TENANT_REFERENCE, EMPLOYEE_ID).createCfgObject(service)

        with(cfgPerson) {
            assertThat(employeeID, equalTo(EMPLOYEE_ID))
        }
    }
}

private fun mockCfgPerson(): CfgPerson {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    val cfgAppRanks = listOf(
        mockCfgAppRank(CFGAdvisors, CFGUser),
        mockCfgAppRank(CFGAgentDesktop, CFGDesigner)
    )

    val cfgAgentInfo = mockCfgAgentInfo()

    return mockCfgPerson(person.employeeId).apply {
        every { configurationService } returns service
        every { externalID } returns person.externalId
        every { firstName } returns person.firstName
        every { lastName } returns person.lastName
        every { userName } returns person.userName
        every { password } returns person.password
        every { passwordHashAlgorithm } returns person.passwordHashAlgorithm
        every { changePasswordOnNextLogin } returns CfgFlag.CFGFalse
        every { emailAddress } returns person.emailAddress
        every { state } returns toCfgObjectState(person.state)
        every { isAgent } returns CfgFlag.CFGTrue
        every { isExternalAuth } returns CfgFlag.CFGFalse
        every { appRanks } returns cfgAppRanks
        every { userProperties } returns mockKeyValueCollection()
        every { agentInfo } returns cfgAgentInfo
        every { folderId } returns DEFAULT_FOLDER_DBID
    }
}

private fun mockCfgAppRank(appType: CfgAppType, appRank: CfgRank): CfgAppRank {
    val cfgAppRank = mockk<CfgAppRank>()
    every { cfgAppRank.appType } returns appType
    every { cfgAppRank.appRank } returns appRank
    return cfgAppRank
}

private fun mockCfgAgentInfo(): CfgAgentInfo {
    val capacityRule = mockCfgScript("capacityRule")
    val contract = mockCfgObjectiveTable("contract")
    val place = mockCfgPlace("place")
    val site = mockCfgFolder(DEFAULT_FOLDER, CFGFolder)

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
