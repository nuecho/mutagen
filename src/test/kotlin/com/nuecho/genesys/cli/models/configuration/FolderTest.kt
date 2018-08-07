package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.configuration.protocol.types.CfgFolderClass.CFGFCDefault
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFolderClass
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
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
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

private const val FOLDER_NAME = "name"
private val folder = Folder(
    name = FOLDER_NAME,
    description = "description",
    folderClass = CFGFCDefault.toShortName(),
    customType = 8,
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class FolderTest : ConfigurationObjectTest(
    configurationObject = folder,
    emptyConfigurationObject = Folder(name = FOLDER_NAME, folder = DEFAULT_FOLDER_REFERENCE),
    mandatoryProperties = emptySet(),
    importedConfigurationObject = Folder(mockCfgFolder())
) {
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() {
        // not implemented, since object has no unchangeable properties
    }

    @Test
    fun `createCfgObject should properly create CfgFolder`() {
        val service = mockConfService()

        staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
            every { service.getObjectDbid(any()) } answers { DEFAULT_OBJECT_DBID }

            objectMockk(ConfigurationObjectRepository).use {
                mockConfigurationObjectRepository()

                val cfgFolder = folder.createCfgObject(service)

                with(cfgFolder) {
                    assertThat(name, equalTo(folder.name))
                    assertThat(description, equalTo(folder.description))
                    assertThat(folderClass, equalTo(toCfgFolderClass(folder.folderClass)))
                    assertThat(customType, equalTo(folder.customType))
                    assertThat(state, equalTo(toCfgObjectState(folder.state)))
                    assertThat(userProperties.asCategorizedProperties(), equalTo(folder.userProperties))
                    assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
                }
            }
        }
    }
}

private fun mockCfgFolder(): CfgFolder {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    return ConfigurationObjectMocks.mockCfgFolder(folder.name).apply {
        every { configurationService } returns service
        every { name } returns folder.name
        every { description } returns folder.description
        every { folderClass } returns toCfgFolderClass(folder.folderClass)
        every { customType } returns folder.customType
        every { state } returns toCfgObjectState(folder.state)
        every { userProperties } returns mockKeyValueCollection()
        every { folderId } returns DEFAULT_OBJECT_DBID
    }
}
