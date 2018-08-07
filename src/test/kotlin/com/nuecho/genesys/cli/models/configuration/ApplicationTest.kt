package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAppPrototype
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgConnInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgPortInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgServer
import com.genesyslab.platform.configuration.protocol.types.CfgAppComponentType.CFGAppComponentUnknown
import com.genesyslab.platform.configuration.protocol.types.CfgAppType.CFGAgentDesktop
import com.genesyslab.platform.configuration.protocol.types.CfgFlag.CFGTrue
import com.genesyslab.platform.configuration.protocol.types.CfgHAType.CFGHTColdStanby
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgTraceMode.CFGTMNone
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgAppPrototype
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgApplication
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgHost
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.AppPrototypeReference
import com.nuecho.genesys.cli.models.configuration.reference.ApplicationReference
import com.nuecho.genesys.cli.models.configuration.reference.HostReference
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.mockk
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test

private const val APP_PROTOTYPE_DBID = 102
private const val APP_PROTOTYPE_VERSION = "1.2.3"
private val application = Application(
    name = "foo",
    appPrototype = AppPrototypeReference("foo"),
    appServers = listOf(
        ConnInfo(
            appParams = "appParams",
            appServer = ApplicationReference("appServer"),
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
        timeout = 2
    ),
    tenants = listOf(DEFAULT_TENANT_REFERENCE),
    workDirectory = "/tmp",
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
)

class ApplicationTest : ConfigurationObjectTest(
    configurationObject = application,
    emptyConfigurationObject = Application(name = "foo"),
    mandatoryProperties = setOf(APP_PROTOTYPE, AUTO_RESTART, REDUNDANCY_TYPE),
    importedConfigurationObject = Application(mockCfgApplication())
) {
    val service = mockConfService()

    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() {
        // not implemented, since object has no unchangeable properties
    }

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
        every { service.retrieveObject(CfgApplication::class.java, any()) } returns null
        ConfServiceExtensionMocks.mockRetrieveHost(service)
        ConfServiceExtensionMocks.mockRetrieveTenant(service)
        ConfServiceExtensionMocks.mockRetrieveAppPrototype(service, APP_PROTOTYPE_DBID, CFGAgentDesktop, APP_PROTOTYPE_VERSION)

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgApplication = application.createCfgObject(service)

            with(cfgApplication) {
                assertThat(appPrototypeDBID, equalTo(APP_PROTOTYPE_DBID))
                assertThat(appServers.toList(), equalTo(
                    application.appServers?.map { it.toCfgConnInfo(this) } as Collection<CfgConnInfo>
                ))
                assertThat(autoRestart.asBoolean(), equalTo(application.autoRestart))
                assertThat(commandLine, equalTo(application.commandLine))
                assertThat(componentType.toShortName(), equalTo(application.componentType))
                assertThat(flexibleProperties.asCategorizedProperties(), equalTo(application.flexibleProperties))
                assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
                assertThat(isPrimary?.asBoolean(), equalTo(application.isPrimary))
                assertThat(name, equalTo(application.name))
                assertThat(options.asCategorizedProperties(), equalTo(application.options))
                assertThat(portInfos.toList(), equalTo(
                    application.portInfos?.map { it.toCfgPortInfo(this) } as Collection<CfgPortInfo>
                ))
                assertThat(redundancyType.toShortName(), equalTo(application.redundancyType))
                assertThat(serverInfo, equalTo(application.serverInfo?.toCfgServer(this)))
                assertThat(state, equalTo(toCfgObjectState(application.state)))
                assertThat(type, equalTo(CFGAgentDesktop))
                assertThat(userProperties.asCategorizedProperties(), equalTo(application.userProperties))
                assertThat(version, equalTo(APP_PROTOTYPE_VERSION))
                assertThat(workDirectory, equalTo(application.workDirectory))
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
    val tenant = ConfigurationObjectMocks.mockCfgTenant("tenant")
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
    every { folderId } returns DEFAULT_OBJECT_DBID
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

    every { cfgServer.attempts } returns 2
    // could be another application but I think that's overkill for unit tests
    every { cfgServer.backupServer } returns null
    every { cfgServer.backupServerDBID } returns null
    every { cfgServer.host } returns host
    every { cfgServer.hostDBID } returns 123
    every { cfgServer.port } returns "8888"
    every { cfgServer.timeout } returns 2

    return cfgServer
}
