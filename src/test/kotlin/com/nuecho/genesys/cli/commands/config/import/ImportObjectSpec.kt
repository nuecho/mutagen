package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPReseller
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.applicationblocks.com.objects.CfgTimeZone
import com.nuecho.genesys.cli.models.ImportPlan
import com.nuecho.genesys.cli.models.ImportPlan.Companion.importConfigurationObject
import com.nuecho.genesys.cli.models.ImportPlanOperation
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveReseller
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTimeZone
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.objectMockk
import io.mockk.use
import io.mockk.verify
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.Test

@Suppress("UnnecessaryAbstractClass")
abstract class ImportObjectSpec(val cfgObject: CfgObject, val configurationObject: ConfigurationObject) {
    val type = configurationObject::class.simpleName!!.toLowerCase()

    fun testImportConfigurationObject(create: Boolean) {
        val retrieveObjectResult = if (create) null else cfgObject
        val service = cfgObject.configurationService as ConfService
        every { service.retrieveObject(cfgObject.javaClass, any()) } returns retrieveObjectResult

        objectMockk(ImportPlan.Companion).use {
            every { ImportPlan.save(any()) } just Runs

            mockRetrieveDefaultObjects(service, cfgObject)

            if (cfgObject !is CfgTenant) {
                mockRetrieveTenant(service)
            }

            objectMockk(ConfigurationObjectRepository).use {
                mockConfigurationObjectRepository()

                val hasImportedObject = importConfigurationObject(ImportPlanOperation(service, configurationObject))
                assertThat(hasImportedObject, `is`(true))
                verify(exactly = 1) { ImportPlan.save(any()) }
            }
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
