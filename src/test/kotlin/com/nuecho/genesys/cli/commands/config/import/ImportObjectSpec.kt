package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPReseller
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.applicationblocks.com.objects.CfgTimeZone
import com.nuecho.genesys.cli.commands.config.import.Import.Companion.importConfigurationObject
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveReseller
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTimeZone
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.objectMockk
import io.mockk.use
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Suppress("UnnecessaryAbstractClass")
abstract class ImportObjectSpec(val cfgObject: CfgObject, val configurationObject: ConfigurationObject) {
    val type = configurationObject::class.simpleName!!.toLowerCase()

    fun testImportConfigurationObject(create: Boolean) {
        val retrieveObjectResult = if (create) null else cfgObject
        val service = cfgObject.configurationService

        objectMockk(Import.Companion).use {
            every { service.retrieveObject(cfgObject.javaClass, any()) } returns retrieveObjectResult
            every { Import.save(any()) } just Runs

            if (cfgObject !is CfgTenant) {
                mockRetrieveTenant(service)
            }

            mockRetrieveDefaultObjects(service, cfgObject)
            val hasImportedObject = importConfigurationObject(configurationObject, service)
            assertTrue(hasImportedObject)
            verify(exactly = 1) { Import.save(any()) }
        }
    }

    @Test
    fun `importing an existing object should try to save it`() {
        testImportConfigurationObject(false)
    }

    @Test
    fun `importing a new object should try to save it`() {
        testImportConfigurationObject(true)
    }
}

private fun mockRetrieveDefaultObjects(service: IConfService, cfgObject: CfgObject) {
    if (cfgObject !is CfgTimeZone) mockRetrieveTimeZone(service)
    if (cfgObject !is CfgGVPReseller) mockRetrieveReseller(service)
}
