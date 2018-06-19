package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPReseller
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTimeZone
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.GregorianCalendar

private const val NAME = "reseller"
private val gvpReseller = GVPReseller(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    startDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse("2018-06-06T21:17:50.105+0000"),
    state = CfgObjectState.CFGEnabled.toShortName(),
    userProperties = defaultProperties()
)

class GVPResellerTest : ConfigurationObjectTest(
    gvpReseller,
    GVPReseller(tenant = DEFAULT_TENANT_REFERENCE, name = NAME),
    GVPReseller(mockCfgGVPReseller())
) {
    init {
        "GVPReseller.updateCfgObject should properly create CfgGVPReseller" {
            val service = mockConfService()
            every { service.retrieveObject(CfgGVPReseller::class.java, any()) } returns null
            mockRetrieveTenant(service)
            mockRetrieveTimeZone(service)

            val cfgGVPReseller = gvpReseller.updateCfgObject(service)

            with(cfgGVPReseller) {
                name shouldBe gvpReseller.name
                state shouldBe ConfigurationObjects.toCfgObjectState(gvpReseller.state)
                userProperties.asCategorizedProperties() shouldBe gvpReseller.userProperties
            }
        }
    }
}

private fun mockCfgGVPReseller() =
    ConfigurationObjectMocks.mockCfgGVPReseller(gvpReseller.name).apply {
        every { displayName } returns null
        every { isParentNSP } returns ConfigurationObjects.toCfgFlag(gvpReseller.isParentNSP)
        every { notes } returns gvpReseller.notes
        every { timeZone } returns null
        every { startDate } returns GregorianCalendar.from(
            ZonedDateTime.ofInstant(gvpReseller.startDate!!.toInstant(), ZoneId.systemDefault())
        )

        every { state } returns toCfgObjectState(gvpReseller.state)
        every { userProperties } returns mockKeyValueCollection()
    }
