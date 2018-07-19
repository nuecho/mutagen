package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.configuration.protocol.types.CfgDataType
import com.genesyslab.platform.configuration.protocol.types.CfgFieldType
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgField
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgDataType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFieldType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
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
    type = CfgDataType.CFGDTChar.toShortName(),
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class FieldTest : ConfigurationObjectTest(
    field,
    Field(DEFAULT_TENANT_REFERENCE, "foo"),
    setOf(FIELD_TYPE),
    Field(mockField())
) {
    val service = mockConfService()

    @Test
    fun `updateCfgObject should properly create CfgField`() {
        mockRetrieveTenant(service)

        staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
            every { service.getObjectDbid(ofType(FolderReference::class)) } answers { FOLDER_OBJECT_DBID }

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
                    assertThat(folderId, equalTo(FOLDER_OBJECT_DBID))
                }
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
    every { folderId } returns DEFAULT_OBJECT_DBID
}
