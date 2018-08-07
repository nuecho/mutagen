package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPIVRProfile
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_IVR_PROFILE_TYPE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
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
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.GregorianCalendar

private const val NAME = "name"
private val SIMPLE_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

private val gvpIVRProfile = GVPIVRProfile(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    displayName = NAME,
    description = "a description",
    notes = "a note",
    type = DEFAULT_IVR_PROFILE_TYPE.toShortName(),
    startServiceDate = SIMPLE_DATE_FORMAT.parse("2018-06-06T21:17:50.105+0000"),
    endServiceDate = SIMPLE_DATE_FORMAT.parse("2019-06-06T21:17:50.105+0000"),
    tfn = listOf("1888", "1877"),
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class GVPIVRProfileTest : ConfigurationObjectTest(
    configurationObject = gvpIVRProfile,
    emptyConfigurationObject = GVPIVRProfile(name = NAME),
    mandatoryProperties = setOf(DISPLAY_NAME, TENANT),
    importedConfigurationObject = GVPIVRProfile(mockCfgGVPIVRProfile())
) {
    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() {
        val cfgGvpIVRProfile = ConfigurationObjectMocks.mockCfgGVPIVRProfile(
            name = gvpIVRProfile.name,
            tenant = ConfigurationObjectMocks.mockCfgTenant("differentTenantName")
        )

        assertThat(
            configurationObject.checkUnchangeableProperties(cfgGvpIVRProfile),
            equalTo(setOf(TENANT))
        )
    }

    @Test
    fun `createCfgObject should properly create CfgGVPIVRProfile`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgGVPIVRProfile::class.java, any()) } returns null
        mockRetrieveTenant(service)

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgGVPIVRProfile = gvpIVRProfile.createCfgObject(service)

            with(cfgGVPIVRProfile) {
                assertThat(name, equalTo(gvpIVRProfile.name))
                assertThat(state, equalTo(ConfigurationObjects.toCfgObjectState(gvpIVRProfile.state)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(gvpIVRProfile.userProperties))
            }
        }
    }
}

private fun mockCfgGVPIVRProfile() = ConfigurationObjectMocks.mockCfgGVPIVRProfile(gvpIVRProfile.name).apply {
    val service = mockConfService()
    mockRetrieveFolderByDbid(service)

    every { configurationService } returns service
    every { customer } returns null
    every { reseller } returns null

    every { displayName } returns gvpIVRProfile.displayName
    every { type } returns DEFAULT_IVR_PROFILE_TYPE
    every { notes } returns gvpIVRProfile.notes
    every { description } returns gvpIVRProfile.description

    every { startServiceDate } returns GregorianCalendar.from(
        ZonedDateTime.ofInstant(gvpIVRProfile.startServiceDate!!.toInstant(), ZoneId.systemDefault())
    )
    every { endServiceDate } returns GregorianCalendar.from(
        ZonedDateTime.ofInstant(gvpIVRProfile.endServiceDate!!.toInstant(), ZoneId.systemDefault())
    )

    every { isProvisioned } returns toCfgFlag(gvpIVRProfile.isProvisioned)

    every { tfn } returns gvpIVRProfile.tfn?.joinToString()
    every { status } returns gvpIVRProfile.status
    every { diDs } returns null

    every { state } returns toCfgObjectState(gvpIVRProfile.state)
    every { userProperties } returns mockKeyValueCollection()
    every { folderId } returns DEFAULT_OBJECT_DBID
}
