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

import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.configuration.protocol.types.CfgFolderClass.CFGFCDefault
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPerson
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgFolderClass
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.mutagen.cli.services.ServiceMocks.mockConfService
import com.nuecho.mutagen.cli.toShortName
import io.mockk.every
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

private const val FOLDER_NAME = "name"
private val folder = Folder(
    name = FOLDER_NAME,
    type = CFGPerson.toShortName(),
    description = "description",
    folderClass = CFGFCDefault.toShortName(),
    customType = 8,
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class FolderTest : ConfigurationObjectTest(
    configurationObject = folder,
    emptyConfigurationObject = Folder(name = FOLDER_NAME, type = CFGPerson.toShortName(), folder = DEFAULT_FOLDER_REFERENCE),
    mandatoryProperties = emptySet(),
    importedConfigurationObject = Folder(mockCfgFolder())
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(folder.folder)
            .toSet()

        assertThat(folder.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        assertUnchangeableProperties(mockCfgFolder(), FOLDER)

    @Test
    fun `createCfgObject should properly create CfgFolder`() {
        val service = mockConfService()
        mockRetrieveTenant(service)
        val cfgFolder = folder.createCfgObject(service)

        with(cfgFolder) {
            assertThat(name, equalTo(folder.name))
            assertThat(type, equalTo(CFGPerson))
            assertThat(description, equalTo(folder.description))
            assertThat(folderClass, equalTo(toCfgFolderClass(folder.folderClass)))
            assertThat(customType, equalTo(folder.customType))
            assertThat(state, equalTo(toCfgObjectState(folder.state)))
            assertThat(userProperties.asCategorizedProperties(), equalTo(folder.userProperties))
            assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
            assertThat(ownerID.dbid, equalTo(DEFAULT_TENANT_DBID))
        }
    }
}

private fun mockCfgFolder(): CfgFolder {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    return ConfigurationObjectMocks.mockCfgFolder(folder.name).apply {
        every { configurationService } returns service
        every { name } returns folder.name
        every { type } returns CFGPerson
        every { description } returns folder.description
        every { folderClass } returns toCfgFolderClass(folder.folderClass)
        every { customType } returns folder.customType
        every { state } returns toCfgObjectState(folder.state)
        every { userProperties } returns mockKeyValueCollection()
        every { folderId } returns DEFAULT_FOLDER_DBID
    }
}
