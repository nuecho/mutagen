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

package com.nuecho.mutagen.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAppPrototype
import com.genesyslab.platform.applicationblocks.com.objects.CfgConnInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgPortInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgServer
import com.genesyslab.platform.configuration.protocol.types.CfgAppComponentType.CFGAppComponentUnknown
import com.genesyslab.platform.configuration.protocol.types.CfgAppType.CFGAgentDesktop
import com.genesyslab.platform.configuration.protocol.types.CfgFlag.CFGTrue
import com.genesyslab.platform.configuration.protocol.types.CfgHAType.CFGHTColdStanby
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgTraceMode.CFGTMNone
import com.nuecho.mutagen.cli.asBoolean
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgAppPrototype
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgApplication
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgHost
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgTraceMode
import com.nuecho.mutagen.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.mutagen.cli.models.configuration.reference.AppPrototypeReference
import com.nuecho.mutagen.cli.models.configuration.reference.ApplicationReference
import com.nuecho.mutagen.cli.models.configuration.reference.HostReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.mutagen.cli.services.ConfigurationObjectRepository
import com.nuecho.mutagen.cli.services.ServiceMocks.mockConfService
import com.nuecho.mutagen.cli.services.retrieveObject
import com.nuecho.mutagen.cli.toShortName
import io.mockk.every
import io.mockk.mockk
import io.mockk.objectMockk
import io.mockk.staticMockk
import io.mockk.use
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test

private const val APP_PROTOTYPE_VERSION = "1.2.3"
private const val APP_SERVER_NAME = "appServer"
private const val BACKUP_SERVER_NAME = "backupServer"
private val application = Application(
    name = "foo",
    appPrototype = AppPrototypeReference("foo"),
    appServers = listOf(
        ConnInfo(
            appParams = "appParams",
            appServer = ApplicationReference(APP_SERVER_NAME),
            charField1 = "1",
            charField2 = "2",
            charField3 = "3",
            charField4 = "4",
            connProtocol = "connProtocol",
            description = "description",
            id = "default",
            longField1 = 1,
            longField2 = 2,
            longField3 = 3,
            longField4 = 4,
            mode = CFGTMNone.toShortName(),
            proxyParams = "proxyParams",
            timeoutLocal = 2,
            timeoutRemote = 2,
            transportParams = "transportParams"
        )
    ),
    autoRestart = true,
    commandLine = "yes",
    componentType = CFGAppComponentUnknown.toShortName(),
    flexibleProperties = defaultProperties(),
    isPrimary = false,
    options = defaultProperties(),
    portInfos = listOf(
        PortInfo(
            appParams = "appParams",
            charField1 = "1",
            charField2 = "2",
            charField3 = "3",
            charField4 = "4",
            connProtocol = "connProtocol",
            description = "description",
            id = "123",
            longField1 = 1,
            longField2 = 2,
            longField3 = 3,
            longField4 = 4,
            port = "8888",
            transportParams = "transportParams"
        )
    ),
    redundancyType = CFGHTColdStanby.toShortName(),
    serverInfo = Server(
        attempts = 2,
        host = HostReference("host"),
        backupServer = ApplicationReference(BACKUP_SERVER_NAME),
        timeout = 2
    ),
    tenants = listOf(DEFAULT_TENANT_REFERENCE),
    workDirectory = "/tmp",
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class ApplicationTest : ConfigurationObjectTest(
    configurationObject = application,
    emptyConfigurationObject = Application(name = "foo"),
    mandatoryProperties = setOf(APP_PROTOTYPE, AUTO_RESTART, REDUNDANCY_TYPE),
    importedConfigurationObject = Application(mockCfgApplication())
) {
    val service = mockConfService()

    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(application.appPrototype)
            .add(application.appServers!![0].appServer)
            .add(application.serverInfo!!.host)
            .add(application.serverInfo!!.backupServer)
            .add(application.tenants)
            .add(application.folder)
            .toSet()

        assertThat(application.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        assertUnchangeableProperties(mockCfgApplication(), FOLDER)

    @Test
    fun `server application missing mandatory server properties should return the missing properties' names`() {
        val configuration = Configuration(
            Metadata(formatName = "JSON", formatVersion = "1.0.0"),
            appPrototypes = listOf(AppPrototype(name = "appPrototype", type = "tserver"))
        )
        val application = Application(
            name = "application", appPrototype = AppPrototypeReference("appPrototype"),
            autoRestart = true,
            redundancyType = CFGHTColdStanby.toShortName()
        )

        every { service.retrieveObject(CfgAppPrototype::class.java, any()) } returns null
        assertThat(application.checkMandatoryProperties(configuration, service), Matchers.equalTo(setOf(COMMAND_LINE, WORK_DIRECTORY)))
    }

    @Test
    fun `updateCfgObject should properly create CfgApplication`() {
        val appServerDbid = 110
        val backupServerDbid = 111
        val hostDbid = 112
        val appPrototypeDbid = 1113

        ConfServiceExtensionMocks.mockRetrieveHost(service, hostDbid)
        ConfServiceExtensionMocks.mockRetrieveTenant(service)
        ConfServiceExtensionMocks.mockRetrieveAppPrototype(service, appPrototypeDbid, CFGAgentDesktop, APP_PROTOTYPE_VERSION)

        staticMockk("com.nuecho.mutagen.cli.services.ConfServiceExtensionsKt").use {
            val appServer = mockCfgApplication(APP_SERVER_NAME, appServerDbid)
            val backupServer = mockCfgApplication(BACKUP_SERVER_NAME, backupServerDbid)
            every { service.retrieveObject(application.reference) } returns null
            every { service.retrieveObject(application.appServers!![0].appServer!!) } returns appServer
            every { service.retrieveObject(application.serverInfo!!.backupServer!!) } returns backupServer

            objectMockk(ConfigurationObjectRepository).use {
                mockConfigurationObjectRepository()
                val cfgApplication = application.createCfgObject(service)

                with(cfgApplication) {
                    assertThat(appPrototypeDBID, equalTo(appPrototypeDbid))
                    assertThat(autoRestart.asBoolean(), equalTo(application.autoRestart))
                    assertThat(commandLine, equalTo(application.commandLine))
                    assertThat(componentType.toShortName(), equalTo(application.componentType))
                    assertThat(flexibleProperties.asCategorizedProperties(), equalTo(application.flexibleProperties))
                    assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
                    assertThat(isPrimary?.asBoolean(), equalTo(application.isPrimary))
                    assertThat(name, equalTo(application.name))
                    assertThat(options.asCategorizedProperties(), equalTo(application.options))
                    assertThat(redundancyType.toShortName(), equalTo(application.redundancyType))
                    assertThat(state, equalTo(toCfgObjectState(application.state)))
                    assertThat(tenantDBIDs.toList(), equalTo(listOf(DEFAULT_TENANT_DBID)))
                    assertThat(type, equalTo(CFGAgentDesktop))
                    assertThat(userProperties.asCategorizedProperties(), equalTo(application.userProperties))
                    assertThat(version, equalTo(APP_PROTOTYPE_VERSION))
                    assertThat(workDirectory, equalTo(application.workDirectory))

                    val actualAppServer = appServers.toList()[0]
                    val expectedAppServer = application.appServers!![0]
                    assertThat(actualAppServer.appParams, equalTo(expectedAppServer.appParams))
                    assertThat(actualAppServer.appServerDBID, equalTo(appServerDbid))
                    assertThat(actualAppServer.charField1, equalTo(expectedAppServer.charField1))
                    assertThat(actualAppServer.charField2, equalTo(expectedAppServer.charField2))
                    assertThat(actualAppServer.charField3, equalTo(expectedAppServer.charField3))
                    assertThat(actualAppServer.charField4, equalTo(expectedAppServer.charField4))
                    assertThat(actualAppServer.connProtocol, equalTo(expectedAppServer.connProtocol))
                    assertThat(actualAppServer.description, equalTo(expectedAppServer.description))
                    assertThat(actualAppServer.id, equalTo(expectedAppServer.id))
                    assertThat(actualAppServer.longField1, equalTo(expectedAppServer.longField1))
                    assertThat(actualAppServer.longField2, equalTo(expectedAppServer.longField2))
                    assertThat(actualAppServer.longField3, equalTo(expectedAppServer.longField3))
                    assertThat(actualAppServer.longField4, equalTo(expectedAppServer.longField4))
                    assertThat(actualAppServer.mode, equalTo(toCfgTraceMode(expectedAppServer.mode)))
                    assertThat(actualAppServer.proxyParams, equalTo(expectedAppServer.proxyParams))
                    assertThat(actualAppServer.timoutLocal, equalTo(expectedAppServer.timeoutLocal))
                    assertThat(actualAppServer.timoutRemote, equalTo(expectedAppServer.timeoutRemote))
                    assertThat(actualAppServer.transportParams, equalTo(expectedAppServer.transportParams))

                    val actualPortInfo = portInfos.toList()[0]
                    val expectedPortInfo = application.portInfos!![0]
                    assertThat(actualPortInfo.appParams, equalTo(expectedPortInfo.appParams))
                    assertThat(actualPortInfo.charField1, equalTo(expectedPortInfo.charField1))
                    assertThat(actualPortInfo.charField2, equalTo(expectedPortInfo.charField2))
                    assertThat(actualPortInfo.charField3, equalTo(expectedPortInfo.charField3))
                    assertThat(actualPortInfo.charField4, equalTo(expectedPortInfo.charField4))
                    assertThat(actualPortInfo.connProtocol, equalTo(expectedPortInfo.connProtocol))
                    assertThat(actualPortInfo.description, equalTo(expectedPortInfo.description))
                    assertThat(actualPortInfo.id, equalTo(expectedPortInfo.id))
                    assertThat(actualPortInfo.longField1, equalTo(expectedPortInfo.longField1))
                    assertThat(actualPortInfo.longField2, equalTo(expectedPortInfo.longField2))
                    assertThat(actualPortInfo.longField3, equalTo(expectedPortInfo.longField3))
                    assertThat(actualPortInfo.longField4, equalTo(expectedPortInfo.longField4))
                    assertThat(actualPortInfo.port, equalTo(expectedPortInfo.port))
                    assertThat(actualPortInfo.transportParams, equalTo(expectedPortInfo.transportParams))

                    val expectedServerInfo = application.serverInfo!!
                    assertThat(serverInfo.attempts, equalTo(expectedServerInfo.attempts))
                    assertThat(serverInfo.hostDBID, equalTo(hostDbid))
                    assertThat(serverInfo.backupServerDBID, equalTo(backupServerDbid))
                    assertThat(serverInfo.timeout, equalTo(expectedServerInfo.timeout))
                }
            }
        }
    }
}

private fun mockCfgApplication() = mockCfgApplication(application.name).apply {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    val connInfo = mockCfgConnInfo()
    val server = mockCfgServer()
    val portInfo = mockCfgPortInfo()
    val tenant = mockCfgTenant("tenant")
    val prototype = mockCfgAppPrototype("foo", 102)

    every { configurationService } returns service

    every { appPrototype } returns prototype
    every { appPrototypeDBID } returns 123
    every { appServers } returns listOf(connInfo)
    every { autoRestart } returns CFGTrue
    every { commandLine } returns application.commandLine
    every { commandLineArguments } returns application.commandLineArguments
    every { componentType } returns CFGAppComponentUnknown
    every { flexibleProperties } returns ConfigurationObjects.toKeyValueCollection(application.flexibleProperties)
    every { folderId } returns DEFAULT_FOLDER_DBID
    every { isPrimary } returns toCfgFlag(application.isPrimary)
    every { options } returns ConfigurationObjects.toKeyValueCollection(application.options)
    every { password } returns application.password
    every { portInfos } returns listOf(portInfo)
    every { redundancyType } returns CFGHTColdStanby
    every { resources } returns null
    every { serverInfo } returns server
    every { shutdownTimeout } returns application.shutdownTimeout
    every { startupTimeout } returns application.startupTimeout
    every { state } returns toCfgObjectState(application.state)
    every { tenantDBIDs } returns listOf(1234)
    every { tenants } returns listOf(tenant)
    every { userProperties } returns mockKeyValueCollection()
    every { workDirectory } returns application.workDirectory
}

private fun mockCfgConnInfo(): CfgConnInfo {
    val cfgConnInfo = mockk<CfgConnInfo>()

    val appServer = mockCfgApplication("appServer")

    every { cfgConnInfo.appParams } returns "appParams"
    every { cfgConnInfo.appServer } returns appServer
    every { cfgConnInfo.appServerDBID } returns DEFAULT_OBJECT_DBID
    every { cfgConnInfo.charField1 } returns "1"
    every { cfgConnInfo.charField2 } returns "2"
    every { cfgConnInfo.charField3 } returns "3"
    every { cfgConnInfo.charField4 } returns "4"
    every { cfgConnInfo.connProtocol } returns "connProtocol"
    every { cfgConnInfo.description } returns "description"
    every { cfgConnInfo.id } returns "default"
    every { cfgConnInfo.longField1 } returns 1
    every { cfgConnInfo.longField2 } returns 2
    every { cfgConnInfo.longField3 } returns 3
    every { cfgConnInfo.longField4 } returns 4
    every { cfgConnInfo.mode } returns CFGTMNone
    every { cfgConnInfo.proxyParams } returns "proxyParams"
    every { cfgConnInfo.timoutLocal } returns 2
    every { cfgConnInfo.timoutRemote } returns 2
    every { cfgConnInfo.transportParams } returns "transportParams"

    return cfgConnInfo
}

private fun mockCfgPortInfo(): CfgPortInfo {
    val cfgPortInfo = mockk<CfgPortInfo>()

    every { cfgPortInfo.appParams } returns "appParams"
    every { cfgPortInfo.charField1 } returns "1"
    every { cfgPortInfo.charField2 } returns "2"
    every { cfgPortInfo.charField3 } returns "3"
    every { cfgPortInfo.charField4 } returns "4"
    every { cfgPortInfo.connProtocol } returns "connProtocol"
    every { cfgPortInfo.description } returns "description"
    every { cfgPortInfo.id } returns "123"
    every { cfgPortInfo.longField1 } returns 1
    every { cfgPortInfo.longField2 } returns 2
    every { cfgPortInfo.longField3 } returns 3
    every { cfgPortInfo.longField4 } returns 4
    every { cfgPortInfo.port } returns "8888"
    every { cfgPortInfo.transportParams } returns "transportParams"

    return cfgPortInfo
}

private fun mockCfgServer(): CfgServer {
    val cfgServer = mockk<CfgServer>()
    val host = mockCfgHost("host")
    val backupServer = mockCfgApplication("backupServer")

    every { cfgServer.attempts } returns 2
    every { cfgServer.backupServer } returns backupServer
    every { cfgServer.backupServerDBID } returns DEFAULT_OBJECT_DBID
    every { cfgServer.host } returns host
    every { cfgServer.hostDBID } returns 123
    every { cfgServer.port } returns "8888"
    every { cfgServer.timeout } returns 2

    return cfgServer
}
