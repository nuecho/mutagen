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
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumeratorValue
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.mutagen.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgEnumeratorValue
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.reference.EnumeratorReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveEnumerator
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.mutagen.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.mutagen.cli.services.ServiceMocks.mockConfService
import com.nuecho.mutagen.cli.toShortName
import io.mockk.every
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val NAME = "enumeratorValue"
private const val ENUMERATOR = "enumerator"
private const val ENUMERATOR_DBID = 102
private val ENUMERATOR_REFERENCE = EnumeratorReference(ENUMERATOR, DEFAULT_TENANT_REFERENCE)
private const val DESCRIPTION = "description"
private const val DISPLAY_NAME = "displayName"

private val enumeratorValue = EnumeratorValue(
    tenant = DEFAULT_TENANT_REFERENCE,
    enumerator = ENUMERATOR_REFERENCE,
    name = NAME,
    description = DESCRIPTION,
    displayName = DISPLAY_NAME,
    isDefault = false,
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = ConfigurationTestData.defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class EnumeratorValueTest : ConfigurationObjectTest(
    configurationObject = enumeratorValue,
    emptyConfigurationObject = EnumeratorValue(
        enumerator = ENUMERATOR_REFERENCE,
        name = NAME
    ),
    mandatoryProperties = setOf(DISPLAY_NAME, TENANT)
) {

    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(enumeratorValue.tenant)
            .add(enumeratorValue.enumerator)
            .add(enumeratorValue.folder)
            .toSet()

        assertThat(enumeratorValue.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        assertUnchangeableProperties(mockCfgEnumeratorValue(mockConfService()), FOLDER)

    @Test
    override fun `initialized object should properly serialize`() {
        val service = mockConfService()

        mockRetrieveFolderByDbid(service)
        mockRetrieveEnumerator(service, ENUMERATOR, ENUMERATOR_DBID)

        val enumeratorValue = EnumeratorValue(mockCfgEnumeratorValue(service))
        checkSerialization(enumeratorValue, "enumeratorvalue")
    }

    @Test
    fun `createCfgObject should properly create CfgEnumeratorValue`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgEnumeratorValue::class.java, any()) } returns null
        mockRetrieveTenant(service)
        mockRetrieveEnumerator(service, ENUMERATOR, ENUMERATOR_DBID)

        val cfgEnumeratorValue = enumeratorValue.createCfgObject(service)

        with(cfgEnumeratorValue) {
            assertThat(name, equalTo(enumeratorValue.name))
            assertThat(displayName, equalTo(enumeratorValue.displayName))
            assertThat(description, equalTo(enumeratorValue.description))
            assertThat(enumeratorDBID, equalTo(ENUMERATOR_DBID))
            assertThat(isDefault, equalTo(toCfgFlag(enumeratorValue.isDefault)))
            assertThat(userProperties.asCategorizedProperties(), equalTo(enumeratorValue.userProperties))
            assertThat(state, equalTo(toCfgObjectState(enumeratorValue.state)))
        }
    }
}

private fun mockCfgEnumeratorValue(service: IConfService): CfgEnumeratorValue {
    val cfgEnumeratorValue = mockCfgEnumeratorValue(name = NAME)
    val userPropertiesMock = mockKeyValueCollection()

    return cfgEnumeratorValue.apply {
        every { configurationService } returns service
        every { description } returns DESCRIPTION
        every { displayName } returns DISPLAY_NAME
        every { isDefault } returns CfgFlag.CFGFalse
        every { tenant.name } returns "tenant"
        every { userProperties } returns userPropertiesMock
        every { state } returns CFGEnabled
        every { folderId } returns DEFAULT_FOLDER_DBID
    }
}
