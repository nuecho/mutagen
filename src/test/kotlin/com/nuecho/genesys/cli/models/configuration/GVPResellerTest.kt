package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPReseller
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTimeZone
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.TimeZoneReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTimeZone
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

private const val NAME = "reseller"
private val gvpReseller = GVPReseller(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    timeZone = TimeZoneReference("GMT", DEFAULT_TENANT_REFERENCE),
    startDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse("2018-06-06T21:17:50.105+0000"),
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class GVPResellerTest : ConfigurationObjectTest(
    configurationObject = gvpReseller,
    emptyConfigurationObject = GVPReseller(tenant = DEFAULT_TENANT_REFERENCE, name = NAME),
    mandatoryProperties = emptySet(),
    importedConfigurationObject = GVPReseller(mockCfgGVPReseller())
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(gvpReseller.tenant)
            .add(gvpReseller.timeZone)
            .add(gvpReseller.folder)
            .toSet()

        assertThat(gvpReseller.getReferences(), equalTo(expected))
    }

    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() {
        // not implemented, since object has no unchangeable properties
    }

    @Test
    fun `createCfgObject should properly create CfgGVPReseller`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgGVPReseller::class.java, any()) } returns null
        mockRetrieveTenant(service)
        mockRetrieveTimeZone(service)

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgGVPReseller = gvpReseller.createCfgObject(service)

            with(cfgGVPReseller) {
                assertThat(tenantDBID, equalTo(DEFAULT_TENANT_DBID))
                assertThat(name, equalTo(gvpReseller.name))
                assertThat(timeZoneDBID, equalTo(DEFAULT_OBJECT_DBID))
                assertThat(startDate.time, equalTo(gvpReseller.startDate))
                assertThat(state, equalTo(toCfgObjectState(gvpReseller.state)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(gvpReseller.userProperties))
                assertThat(folderId, equalTo(ConfigurationObjectMocks.DEFAULT_FOLDER_DBID))
            }
        }
    }
}

private fun mockCfgGVPReseller() =
    ConfigurationObjectMocks.mockCfgGVPReseller(gvpReseller.name).apply {
        val service = mockConfService()
        val timezone = mockCfgTimeZone(name = gvpReseller.timeZone!!.primaryKey)
        mockRetrieveFolderByDbid(service)

        every { configurationService } returns service
        every { displayName } returns null
        every { isParentNSP } returns ConfigurationObjects.toCfgFlag(gvpReseller.isParentNSP)
        every { notes } returns gvpReseller.notes
        every { timeZone } returns timezone
        every { startDate } returns GregorianCalendar.from(
            ZonedDateTime.ofInstant(gvpReseller.startDate!!.toInstant(), ZoneId.systemDefault())
        )

        every { state } returns toCfgObjectState(gvpReseller.state)
        every { userProperties } returns mockKeyValueCollection()
        every { folderId } returns DEFAULT_OBJECT_DBID
    }
