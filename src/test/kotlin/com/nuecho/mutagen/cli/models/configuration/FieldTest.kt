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

import com.genesyslab.platform.configuration.protocol.types.CfgDataType.CFGDTChar
import com.genesyslab.platform.configuration.protocol.types.CfgDataType.CFGDTDateTime
import com.genesyslab.platform.configuration.protocol.types.CfgFieldType
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgFlag.CFGTrue
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.mutagen.cli.asBoolean
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgField
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgDataType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgFieldType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.mutagen.cli.services.ConfigurationObjectRepository
import com.nuecho.mutagen.cli.services.ServiceMocks.mockConfService
import com.nuecho.mutagen.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

private val field = Field(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = "foo",
    defaultValue = "foo",
    description = "description",
    fieldType = CfgFieldType.CFGFTInfoDigits.toShortName(),
    isNullable = CfgFlag.CFGFalse.asBoolean(),
    isPrimaryKey = CfgFlag.CFGFalse.asBoolean(),
    isUnique = CfgFlag.CFGFalse.asBoolean(),
    length = 3,
    type = CFGDTChar.toShortName(),
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class FieldTest : ConfigurationObjectTest(
    configurationObject = field,
    emptyConfigurationObject = Field(DEFAULT_TENANT_REFERENCE, "foo"),
    mandatoryProperties = setOf(FIELD_TYPE, IS_NULLABLE, IS_PRIMARY_KEY, IS_UNIQUE, TYPE),
    importedConfigurationObject = Field(mockField())
) {
    val service = mockConfService()

    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(field.tenant)
            .add(field.folder)
            .toSet()

        assertThat(field.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        mockCfgField(name = field.name).let {
            every { it.type } returns CFGDTDateTime
            every { it.length } returns 4
            every { it.isNullable } returns CFGTrue
            every { it.isPrimaryKey } returns CFGTrue
            every { it.isUnique } returns CFGTrue
            assertUnchangeableProperties(it, FOLDER, TYPE, LENGTH, IS_NULLABLE, IS_PRIMARY_KEY, IS_UNIQUE)
        }

    @Test
    fun `updateCfgObject should properly create CfgField`() {
        mockRetrieveTenant(service)

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgField = field.createCfgObject(service)

            with(cfgField) {
                assertThat(name, equalTo(field.name))
                assertThat(description, equalTo(field.description))
                assertThat(defaultValue, equalTo(field.defaultValue))
                assertThat(fieldType, equalTo(toCfgFieldType(field.fieldType)))
                assertThat(isNullable, equalTo(toCfgFlag(field.isNullable)))
                assertThat(isPrimaryKey, equalTo(toCfgFlag(field.isPrimaryKey)))
                assertThat(isUnique, equalTo(toCfgFlag(field.isUnique)))
                assertThat(length, equalTo(field.length))
                assertThat(type, equalTo(toCfgDataType(field.type)))
                assertThat(state, equalTo(toCfgObjectState(field.state)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(field.userProperties))
                assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
            }
        }
    }
}

private fun mockField() = mockCfgField(field.name).apply {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    every { configurationService } returns service
    every { description } returns field.description
    every { defaultValue } returns field.defaultValue
    every { fieldType } returns toCfgFieldType(field.fieldType)
    every { isNullable } returns toCfgFlag(field.isNullable)
    every { isPrimaryKey } returns toCfgFlag(field.isPrimaryKey)
    every { isUnique } returns toCfgFlag(field.isUnique)
    every { length } returns field.length
    every { type } returns toCfgDataType(field.type!!)
    every { state } returns toCfgObjectState(field.state)
    every { userProperties } returns mockKeyValueCollection()
    every { folderId } returns DEFAULT_FOLDER_DBID
}
