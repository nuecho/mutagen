package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgHost
import com.genesyslab.platform.configuration.protocol.types.CfgHostType.CFGNetworkServer
import com.genesyslab.platform.configuration.protocol.types.CfgOSType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGApplication
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgApplication
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgHost
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgOS
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgHostType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.reference.ApplicationReference
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.staticMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val APPLICATION_NAME = "application"
private const val NAME = "host"
private const val IP_ADDRESS = "10.0.1.249"
private const val LCA_PORT = "4999"

private val host = Host(
    name = NAME,
    ipAddress = IP_ADDRESS,
    lcaPort = LCA_PORT,
    osInfo = OS("windows", "8"),
    scs = ApplicationReference(APPLICATION_NAME),
    type = "networkserver",
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = ConfigurationTestData.defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class HostTest : ConfigurationObjectTest(
    host,
    Host(name = NAME),
    setOf("lcaPort", "osInfo", TYPE)
) {
    @Test
    override fun `initialized object should properly serialize`() {
        val service = mockConfService()
        mockRetrieveFolderByDbid(service)

        val application = mockCfgApplication(APPLICATION_NAME)

        objectMockk(ConfigurationObjects).use {
            every {
                service.retrieveObject(CFGApplication, DEFAULT_OBJECT_DBID)
            } returns application

            val host = Host(mockCfgHost(service))
            checkSerialization(host, "host")
        }
    }

    @Test
    fun `updateCfgObject should properly create CfgHost`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgHost::class.java, any()) } returns null

        staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
            every { service.getObjectDbid(any()) } answers { DEFAULT_OBJECT_DBID }

            objectMockk(ConfigurationObjectRepository).use {
                mockConfigurationObjectRepository()

                val cfgHost = host.createCfgObject(service)

                with(cfgHost) {
                    assertThat(name, equalTo(host.name))
                    assertThat(iPaddress, equalTo(host.ipAddress))
                    assertThat(lcaPort, equalTo(host.lcaPort))
                    assertThat(oSinfo, equalTo(host.osInfo?.toCfgOs(service, cfgHost)))
                    assertThat(scsdbid, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(type, equalTo(toCfgHostType(host.type)))

                    assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
                    assertThat(state, equalTo(toCfgObjectState(host.state)))
                    assertThat(userProperties.asCategorizedProperties(), equalTo(host.userProperties))
                }
            }
        }
    }
}

private fun mockCfgHost(service: IConfService): CfgHost {
    mockRetrieveFolderByDbid(service)

    val cfgHost = mockCfgHost(name = NAME)
    val osInfoMock = mockCfgOS(CfgOSType.CFGWindows, "8")
    val userPropertiesMock = mockKeyValueCollection()

    return cfgHost.apply {
        every { configurationService } returns service
        every { iPaddress } returns IP_ADDRESS
        every { lcaPort } returns LCA_PORT
        every { oSinfo } returns osInfoMock
        every { scsdbid } returns DEFAULT_OBJECT_DBID
        every { resources } returns null
        every { type } returns CFGNetworkServer

        every { folderId } returns DEFAULT_OBJECT_DBID
        every { state } returns CFGEnabled
        every { userProperties } returns userPropertiesMock
    }
}
