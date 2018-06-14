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
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.objectMockk
import io.mockk.use
import io.mockk.verify

abstract class ImportObjectSpec(cfgObject: CfgObject, configurationObject: ConfigurationObject) : StringSpec() {
    init {
        val type = configurationObject::class.simpleName!!.toLowerCase()

        "importing an existing $type should do nothing" {
            val service = cfgObject.configurationService
            every { service.retrieveObject(cfgObject.javaClass, any()) } returns cfgObject

            if (cfgObject !is CfgTenant) {
                mockRetrieveTenant(service)
            }
            mockRetrieveDefaultObjects(service, cfgObject)

            objectMockk(Import.Companion).use {
                val hasImportedObject = importConfigurationObject(configurationObject, service)
                hasImportedObject shouldBe false
                verify(exactly = 0) { Import.save(any()) }
            }
        }

        "importing a new $type should try to save it" {
            objectMockk(Import.Companion).use {
                val service = cfgObject.configurationService
                every { service.retrieveObject(cfgObject.javaClass, any()) } returns null
                every { Import.save(any()) } just Runs

                if (cfgObject !is CfgTenant) {
                    mockRetrieveTenant(service)
                }

                mockRetrieveDefaultObjects(service, cfgObject)

                val hasImportedObject = importConfigurationObject(configurationObject, service)
                hasImportedObject shouldBe true
                verify(exactly = 1) { Import.save(ofType(cfgObject.javaClass.kotlin)) }
            }
        }
    }

    private fun mockRetrieveDefaultObjects(service: IConfService, cfgObject: CfgObject) {
        if (cfgObject !is CfgTimeZone) mockRetrieveTimeZone(service)
        if (cfgObject !is CfgGVPReseller) mockRetrieveReseller(service)
    }
}
