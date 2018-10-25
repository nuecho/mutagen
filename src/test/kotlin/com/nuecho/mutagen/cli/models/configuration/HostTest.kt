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

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgHost
import com.genesyslab.platform.configuration.protocol.types.CfgHostType.CFGAgentWorkstation
import com.genesyslab.platform.configuration.protocol.types.CfgHostType.CFGNetworkServer
import com.genesyslab.platform.configuration.protocol.types.CfgOSType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGApplication
import com.nuecho.mutagen.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgApplication
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgHost
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgOS
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgHostType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgOsType
import com.nuecho.mutagen.cli.models.configuration.reference.ApplicationReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.mutagen.cli.services.ConfigurationObjectRepository
import com.nuecho.mutagen.cli.services.ServiceMocks.mockConfService
import com.nuecho.mutagen.cli.services.getObjectDbid
import com.nuecho.mutagen.cli.toShortName
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
    configurationObject = host,
    emptyConfigurationObject = Host(name = NAME),
    mandatoryProperties = setOf("lcaPort", "osInfo", TYPE)
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(host.scs)
            .add(host.folder)
            .toSet()

        assertThat(host.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        mockCfgHost(name = host.name).let {
            every { it.type } returns CFGAgentWorkstation
            assertUnchangeableProperties(it, FOLDER, TYPE)
        }

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

        staticMockk("com.nuecho.mutagen.cli.services.ConfServiceExtensionsKt").use {
            val scsDbid = 102
            every { service.getObjectDbid(host.scs) } answers { scsDbid }

            objectMockk(ConfigurationObjectRepository).use {
                mockConfigurationObjectRepository()

                val cfgHost = host.createCfgObject(service)

                with(cfgHost) {
                    assertThat(name, equalTo(host.name))
                    assertThat(iPaddress, equalTo(host.ipAddress))
                    assertThat(lcaPort, equalTo(host.lcaPort))
                    assertThat(oSinfo.oStype, equalTo(toCfgOsType(host.osInfo!!.type)))
                    assertThat(oSinfo.oSversion, equalTo(host.osInfo!!.version))
                    assertThat(scsdbid, equalTo(scsDbid))
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
    val applicationMock = mockCfgApplication(APPLICATION_NAME)
    val userPropertiesMock = mockKeyValueCollection()

    return cfgHost.apply {
        every { configurationService } returns service
        every { iPaddress } returns IP_ADDRESS
        every { lcaPort } returns LCA_PORT
        every { oSinfo } returns osInfoMock
        every { scsdbid } returns DEFAULT_OBJECT_DBID
        every { scs } returns applicationMock
        every { resources } returns null
        every { type } returns CFGNetworkServer

        every { folderId } returns DEFAULT_FOLDER_DBID
        every { state } returns CFGEnabled
        every { userProperties } returns userPropertiesMock
    }
}
