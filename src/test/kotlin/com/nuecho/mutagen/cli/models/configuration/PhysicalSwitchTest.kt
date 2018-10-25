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

import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgSwitchType.CFG3511ProtocolInterface
import com.genesyslab.platform.configuration.protocol.types.CfgSwitchType.CFGFujitsu
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgPhysicalSwitch
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgSwitchType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.mutagen.cli.services.ConfigurationObjectRepository
import com.nuecho.mutagen.cli.services.ServiceMocks.mockConfService
import com.nuecho.mutagen.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private val physicalSwitch = PhysicalSwitch(
    name = "foo",
    type = CFGFujitsu.toShortName(),
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class PhysicalSwitchTest : ConfigurationObjectTest(
    configurationObject = physicalSwitch,
    emptyConfigurationObject = PhysicalSwitch("foo"),
    mandatoryProperties = setOf(TYPE),
    importedConfigurationObject = PhysicalSwitch(mockPhysicalSwitch())
) {
    val service = mockConfService()

    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(physicalSwitch.folder)
            .toSet()

        assertThat(physicalSwitch.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        mockCfgPhysicalSwitch(name = physicalSwitch.name).let {
            every { it.type } returns CFG3511ProtocolInterface
            assertUnchangeableProperties(it, FOLDER, TYPE)
        }

    @Test
    fun `createCfgObject should properly create CfgPhysicalSwitch`() {
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgPhysicalSwitch = physicalSwitch.createCfgObject(service)

            with(cfgPhysicalSwitch) {
                assertThat(name, equalTo(physicalSwitch.name))
                assertThat(type, equalTo(toCfgSwitchType(physicalSwitch.type)))
                assertThat(state, equalTo(toCfgObjectState(physicalSwitch.state)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(physicalSwitch.userProperties))
            }
        }
    }
}

private fun mockPhysicalSwitch() = mockCfgPhysicalSwitch(physicalSwitch.name).apply {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    every { configurationService } returns service
    every { type } returns toCfgSwitchType(physicalSwitch.type)
    every { state } returns toCfgObjectState(physicalSwitch.state)
    every { userProperties } returns mockKeyValueCollection()
    every { folderId } returns DEFAULT_FOLDER_DBID
}
