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

import com.genesyslab.platform.applicationblocks.com.objects.CfgIVR
import com.genesyslab.platform.configuration.protocol.types.CfgIVRType.CFGIVRTAgility
import com.genesyslab.platform.configuration.protocol.types.CfgIVRType.CFGIVRTAmerex
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_NAME
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgApplication
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgIvr
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgIVRType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.ApplicationReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveApplication
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val NAME = "ivr-name"
private const val IVR_SERVER_NAME = "ivr-server"
private val IVR_TYPE = CFGIVRTAgility

private val ivr = Ivr(
    name = NAME,
    description = "my-ivr-description",
    ivrServer = ApplicationReference(IVR_SERVER_NAME),
    tenant = DEFAULT_TENANT_REFERENCE,
    type = IVR_TYPE.toShortName(),
    version = "1.2.3",
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class IvrTest : ConfigurationObjectTest(
    configurationObject = ivr,
    emptyConfigurationObject = Ivr(name = NAME),
    mandatoryProperties = setOf(TENANT, TYPE, VERSION),
    importedConfigurationObject = Ivr(mockCfgIvr())
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(ivr.ivrServer)
            .add(ivr.tenant)
            .add(ivr.folder)
            .toSet()

        assertThat(ivr.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        mockCfgIvr(ivr.name).let {
            val differentTenant = mockCfgTenant("differentTenant")
            every { it.type } returns CFGIVRTAmerex
            every { it.tenant } returns differentTenant
            assertUnchangeableProperties(it, FOLDER, TENANT, TYPE)
        }

    @Test
    fun `updateCfgObject should properly create CfgIVR`() {
        val service = mockConfService()
        val ivrServerDbid = 102

        every { service.retrieveObject(CfgIVR::class.java, any()) } returns null
        mockRetrieveTenant(service)
        mockRetrieveApplication(service, ivrServerDbid)

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()

            val cfgIvr = ivr.createCfgObject(service)

            with(cfgIvr) {
                assertThat(description, equalTo(ivr.description))
                assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
                assertThat(ivrServerDBID, equalTo(ivrServerDbid))
                assertThat(name, equalTo(ivr.name))
                assertThat(state, equalTo(toCfgObjectState(ivr.state)))
                assertThat(tenantDBID, equalTo(DEFAULT_TENANT_DBID))
                assertThat(type, equalTo(toCfgIVRType(ivr.type)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(ivr.userProperties))
                assertThat(version, equalTo(ivr.version))
            }
        }
    }
}

private fun mockCfgIvr(): CfgIVR {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    val cfgTenant = mockCfgTenant(name = DEFAULT_TENANT_NAME)
    val cfgApplication = mockCfgApplication(name = IVR_SERVER_NAME)
    val cfgIvr = mockCfgIvr(name = NAME)
    val userPropertiesMock = mockKeyValueCollection()

    return cfgIvr.apply {
        every { configurationService } returns service
        every { tenant } returns cfgTenant
        every { description } returns ivr.description
        every { type } returns IVR_TYPE
        every { version } returns ivr.version
        every { ivrServer } returns cfgApplication
        every { folderId } returns DEFAULT_FOLDER_DBID
        every { state } returns CFGEnabled
        every { userProperties } returns userPropertiesMock
    }
}
