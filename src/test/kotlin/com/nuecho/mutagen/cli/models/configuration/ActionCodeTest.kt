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

import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.configuration.protocol.types.CfgActionCodeType.CFGTransfer
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.SUBCODE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.SUBNAME
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgActionCode
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgSubcode
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgActionCodeType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.mutagen.cli.services.ServiceMocks.mockConfService
import com.nuecho.mutagen.cli.toShortName
import io.mockk.every
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test

private const val NAME = "name"
private val TYPE = CFGTransfer.toShortName()
private val actionCode = ActionCode(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    type = TYPE,
    code = "code",
    subcodes = mapOf(SUBNAME to SUBCODE),
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class ActionCodeTest : ConfigurationObjectTest(
    configurationObject = actionCode,
    emptyConfigurationObject = ActionCode(tenant = DEFAULT_TENANT_REFERENCE, name = NAME, type = TYPE),
    mandatoryProperties = setOf(CODE),
    importedConfigurationObject = ActionCode(mockCfgActionCode())
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(actionCode.tenant)
            .add(actionCode.folder)
            .toSet()

        assertThat(actionCode.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        assertUnchangeableProperties(mockCfgActionCode(), FOLDER)

    @Test
    fun `createCfgObject should properly create CfgActionCode`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgActionCode::class.java, any()) } returns null
        mockRetrieveTenant(service)

        val cfgActionCode = actionCode.createCfgObject(service)

        with(cfgActionCode) {
            assertThat(name, equalTo(actionCode.name))
            assertThat(type, equalTo(toCfgActionCodeType(actionCode.type)))
            assertThat(code, equalTo(actionCode.code))
            assertThat(subcodes, hasSize(1))

            with(subcodes.iterator().next()) {
                assertThat(name, equalTo(SUBNAME))
                assertThat(code, equalTo(SUBCODE))
            }

            assertThat(state, equalTo(toCfgObjectState(actionCode.state)))
            assertThat(userProperties.asCategorizedProperties(), equalTo(actionCode.userProperties))
        }
    }
}

private fun mockCfgActionCode(): CfgActionCode {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    val cfgActionCode = mockCfgActionCode(actionCode.name)
    val subcode = mockCfgSubcode()

    return cfgActionCode.apply {
        every { configurationService } returns service
        every { type } returns toCfgActionCodeType(actionCode.type)
        every { code } returns actionCode.code
        every { subcodes } returns listOf(subcode)
        every { state } returns toCfgObjectState(actionCode.state)
        every { userProperties } returns mockKeyValueCollection()
        every { folderId } returns DEFAULT_FOLDER_DBID
    }
}
