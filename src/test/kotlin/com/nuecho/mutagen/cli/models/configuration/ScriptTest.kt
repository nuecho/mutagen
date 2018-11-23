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

import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgScriptType.CFGBusinessProcess
import com.genesyslab.platform.configuration.protocol.types.CfgScriptType.CFGSchedule
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgScriptType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.mutagen.cli.services.ServiceMocks.mockConfService
import com.nuecho.mutagen.cli.toShortName
import io.mockk.every
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val NAME = "name"
private val script = Script(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    type = CFGBusinessProcess.toShortName(),
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class ScriptTest : ConfigurationObjectTest(
    configurationObject = script,
    emptyConfigurationObject = Script(tenant = DEFAULT_TENANT_REFERENCE, name = NAME),
    mandatoryProperties = setOf(TYPE),
    importedConfigurationObject = Script(mockCfgScript())
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(script.tenant)
            .add(script.folder)
            .toSet()

        assertThat(script.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        mockCfgScript(name = script.name).let {
            every { it.type } returns CFGSchedule
            assertUnchangeableProperties(it, FOLDER, TYPE)
        }

    @Test
    fun `createCfgObject should properly create CfgScript`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgScript::class.java, any()) } returns null
        mockRetrieveTenant(service)

        val cfgScript = script.createCfgObject(service)

        with(cfgScript) {
            assertThat(name, equalTo(script.name))
            assertThat(index, equalTo(script.index))
            assertThat(state, equalTo(toCfgObjectState(script.state)))
            assertThat(type, equalTo(toCfgScriptType(script.type)))
            assertThat(userProperties.asCategorizedProperties(), equalTo(script.userProperties))
        }
    }
}

private fun mockCfgScript() = mockCfgScript(script.name).apply {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    every { configurationService } returns service
    every { state } returns CFGEnabled
    every { type } returns toCfgScriptType(script.type)
    every { index } returns script.index
    every { resources } returns null
    every { userProperties } returns mockKeyValueCollection()
    every { folderId } returns DEFAULT_FOLDER_DBID
}
