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

import com.genesyslab.platform.applicationblocks.com.objects.CfgAppPrototype
import com.genesyslab.platform.configuration.protocol.types.CfgAppType.CFGAgentDesktop
import com.genesyslab.platform.configuration.protocol.types.CfgAppType.CFGMaxAppType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgAppPrototype
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgAppType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

const val OPTIONS_NUMBER = 123
const val OPTIONS_STRING = "dude"

private val appPrototype = AppPrototype(
    name = "foo",
    type = CFGAgentDesktop.toShortName(),
    version = "1.2.3",
    options = mapOf(
        "option-section" to mapOf(
            "option-number" to OPTIONS_NUMBER,
            "option-string" to OPTIONS_STRING
        )
    ),
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class AppPrototypeTest : ConfigurationObjectTest(
    configurationObject = appPrototype,
    emptyConfigurationObject = AppPrototype("foo"),
    mandatoryProperties = setOf(TYPE, VERSION),
    importedConfigurationObject = AppPrototype(mockAppPrototype())
) {
    val service = mockConfService()

    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(appPrototype.folder)
            .toSet()

        assertThat(appPrototype.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        mockCfgAppPrototype(
            name = appPrototype.name,
            type = CFGMaxAppType,
            version = "different-${appPrototype.version}"
        ).let {
            every { it.options } returns null
            every { it.userProperties } returns null
            assertUnchangeableProperties(it, FOLDER, TYPE, VERSION)
        }

    @Test
    fun `updateCfgObject should properly create CfgAppPrototype`() {
        every { service.retrieveObject(CfgAppPrototype::class.java, any()) } returns null

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgAppPrototype = appPrototype.createCfgObject(service)

            with(cfgAppPrototype) {
                assertThat(name, equalTo(appPrototype.name))
                assertThat(type, equalTo(toCfgAppType(appPrototype.type)))
                assertThat(version, equalTo(appPrototype.version))
                assertThat(options.asCategorizedProperties(), equalTo(appPrototype.options))
                assertThat(state, equalTo(toCfgObjectState(appPrototype.state)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(appPrototype.userProperties))
                assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
            }
        }
    }
}

private fun mockAppPrototype() = mockCfgAppPrototype(
    name = appPrototype.name,
    dbid = 102,
    type = CFGAgentDesktop,
    version = "1.2.3"
).apply {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    every { configurationService } returns service
    every { options } returns ConfigurationObjects.toKeyValueCollection(appPrototype.options)
    every { state } returns toCfgObjectState(appPrototype.state)
    every { userProperties } returns mockKeyValueCollection()
    every { folderId } returns DEFAULT_FOLDER_DBID
}
