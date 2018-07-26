package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAppPrototype
import com.genesyslab.platform.configuration.protocol.types.CfgAppType.CFGAgentDesktop
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgAppPrototype
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgAppType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
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
    appPrototype,
    AppPrototype("foo"),
    setOf(TYPE, VERSION),
    AppPrototype(mockAppPrototype())
) {
    val service = mockConfService()

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

private fun mockAppPrototype() = mockCfgAppPrototype(appPrototype.name).apply {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    every { configurationService } returns service
    every { type } returns toCfgAppType(appPrototype.type)
    every { version } returns appPrototype.version
    every { options } returns ConfigurationObjects.toKeyValueCollection(appPrototype.options)
    every { state } returns toCfgObjectState(appPrototype.state)
    every { userProperties } returns mockKeyValueCollection()
    every { folderId } returns DEFAULT_OBJECT_DBID
}
