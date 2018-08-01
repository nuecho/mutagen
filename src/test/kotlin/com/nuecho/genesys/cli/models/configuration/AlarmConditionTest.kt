package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAlarmCondition
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.configuration.protocol.types.CfgAlarmCategory.CFGACMajor
import com.genesyslab.platform.configuration.protocol.types.CfgFlag.CFGFalse
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgScriptType.CFGAlarmDetection
import com.genesyslab.platform.configuration.protocol.types.CfgScriptType.CFGAlarmReaction
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_NAME
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgAlarmCondition
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgApplication
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgDetectEvent
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgRemovalEvent
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgAppType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgSelectionMode
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.AlarmConditionScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.ApplicationReference
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveApplication
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val APPLICATION_NAME = "tserverApp"
private const val NAME = "alarmCondition"
private const val DESCRIPTION = "description"
private const val CLEARANCE_TIMEOUT = 10
private const val SELECTION_MODE = "smbyany"
private const val SCRIPT_NAME1 = "script1"
private const val SCRIPT_NAME2 = "script2"
private const val SCRIPT_NAME3 = "script3"
private val DETECT_EVENT = DetectEvent(
    ApplicationReference(APPLICATION_NAME),
    "tserver",
    2,
    SELECTION_MODE
)
private val REMOVAL_EVENT = RemovalEvent(3, SELECTION_MODE)

private val alarmCondition = AlarmCondition(
    name = NAME,
    alarmDetectEvent = DETECT_EVENT,
    alarmDetectScript = AlarmConditionScriptReference(SCRIPT_NAME1, DEFAULT_TENANT_REFERENCE),
    alarmRemovalEvent = REMOVAL_EVENT,
    category = "acmajor",
    clearanceScripts = listOf(AlarmConditionScriptReference(SCRIPT_NAME2, DEFAULT_TENANT_REFERENCE)),
    clearanceTimeout = CLEARANCE_TIMEOUT,
    description = DESCRIPTION,
    isMasked = false,
    reactionScripts = listOf(AlarmConditionScriptReference(SCRIPT_NAME3, DEFAULT_TENANT_REFERENCE)),
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class AlarmConditionTest : ConfigurationObjectTest(
    configurationObject = alarmCondition,
    emptyConfigurationObject = AlarmCondition(NAME),
    mandatoryProperties = setOf(ALARM_DETECT_EVENT, ALARM_DETECT_EVENT_LOG_EVENT_ID, CATEGORY),
    importedConfigurationObject = AlarmCondition(mockAlarmCondition())
) {
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() {
        // not implemented, since object has no unchangeable properties
    }

    val service = mockConfService()
    val application = mockCfgApplication(APPLICATION_NAME)
    val tenant = mockCfgTenant(DEFAULT_TENANT_NAME)
    val script1 = mockCfgScript(name = SCRIPT_NAME1, dbid = 102, type = CFGAlarmReaction)
    val script2 = mockCfgScript(name = SCRIPT_NAME2, dbid = 103, type = CFGAlarmDetection)
    val script3 = mockCfgScript(name = SCRIPT_NAME3, dbid = 104, type = CFGAlarmDetection)

    @Test
    fun `createCfgObject should properly create CfgAlarmCondition`() {
        mockRetrieveTenant(service)
        mockRetrieveApplication(service)

        every { service.retrieveObject(CfgAlarmCondition::class.java, any()) } returns null
        every {
            service.retrieveObject(CfgScript::class.java, any())
        } returns script1 andThen script2 andThen script3

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgAlarmCondition = alarmCondition.createCfgObject(service)

            with(cfgAlarmCondition) {
                assertThat(alarmDetectEvent.appDBID, equalTo(DEFAULT_OBJECT_DBID))
                assertThat(alarmDetectEvent.appType, equalTo(toCfgAppType(DETECT_EVENT.appType)))
                assertThat(alarmDetectEvent.logEventID, equalTo(DETECT_EVENT.logEventID))
                assertThat(alarmDetectEvent.selectionMode, equalTo(toCfgSelectionMode(DETECT_EVENT.selectionMode)))
                assertThat(alarmDetectScriptDBID, equalTo(script1.dbid))
                assertThat(alarmRemovalEvent.logEventID, equalTo(REMOVAL_EVENT.logEventID))
                assertThat(alarmRemovalEvent.selectionMode, equalTo(toCfgSelectionMode(REMOVAL_EVENT.selectionMode)))
                assertThat(category, equalTo(CFGACMajor))
                assertThat(clearanceScriptDBIDs.toList()[0], equalTo(script2.dbid))
                assertThat(clearanceTimeout, equalTo(CLEARANCE_TIMEOUT))
                assertThat(description, equalTo(DESCRIPTION))
                assertThat(isMasked, equalTo(CFGFalse))
                assertThat(reactionScriptDBIDs.toList()[0], equalTo(script3.dbid))

                assertThat(name, equalTo(alarmCondition.name))
                assertThat(state, equalTo(toCfgObjectState(alarmCondition.state)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(alarmCondition.userProperties))
            }
        }
    }
}

private fun mockAlarmCondition() = mockCfgAlarmCondition(alarmCondition.name).apply {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    val mockApplication = mockCfgApplication(APPLICATION_NAME)
    val mockDetectEvent = mockCfgDetectEvent(
        mockApplication,
        toCfgAppType("tserver"),
        2,
        toCfgSelectionMode(SELECTION_MODE)
    )
    val mockRemovalEvent = mockCfgRemovalEvent(3, toCfgSelectionMode(SELECTION_MODE))

    val script1 = mockCfgScript(name = SCRIPT_NAME1, dbid = 102, type = CFGAlarmReaction)
    val script2 = mockCfgScript(name = SCRIPT_NAME2, dbid = 103, type = CFGAlarmReaction)
    val script3 = mockCfgScript(name = SCRIPT_NAME3, dbid = 104, type = CFGAlarmDetection)

    every { configurationService } returns service

    every { alarmDetectEvent } returns mockDetectEvent
    every { alarmDetectScript } returns script1
    every { alarmRemovalEvent } returns mockRemovalEvent
    every { category } returns CFGACMajor
    every { clearanceScripts } returns listOf(script2)
    every { clearanceTimeout } returns CLEARANCE_TIMEOUT
    every { description } returns DESCRIPTION
    every { isMasked } returns CFGFalse
    every { reactionScripts } returns listOf(script3)

    every { state } returns toCfgObjectState(alarmCondition.state)
    every { userProperties } returns mockKeyValueCollection()
    every { folderId } returns DEFAULT_OBJECT_DBID
}
