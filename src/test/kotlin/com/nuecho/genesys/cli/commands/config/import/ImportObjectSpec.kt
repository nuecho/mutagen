package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.nuecho.genesys.cli.commands.config.import.Import.Companion.importConfigurationObjects
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.objectMockk
import io.mockk.use
import io.mockk.verify

abstract class ImportObjectSpec(cfgObject: CfgObject, objects: List<ConfigurationObject>) : StringSpec() {
    init {
        val type = objects.first()::class.simpleName!!.toLowerCase()

        assert(objects.size > 1)

        "importing an existing $type should do nothing" {
            val service = cfgObject.configurationService
            every { service.retrieveObject(cfgObject.javaClass, any()) } returns cfgObject

            if (cfgObject !is CfgTenant) {
                val tenant = mockCfgTenant(DEFAULT_TENANT)
                every { service.retrieveObject(CfgTenant::class.java, any()) } returns tenant
            }

            objectMockk(Import.Companion).use {
                val count = importConfigurationObjects(objects, service)
                count shouldBe 0
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

                val count = importConfigurationObjects(objects.subList(0, 1), service)
                count shouldBe 1
                verify(exactly = 1) { Import.save(ofType(cfgObject.javaClass.kotlin)) }
            }
        }

        "importing multiple $type should try to save all of them" {
            val service = cfgObject.configurationService
            every { service.retrieveObject(cfgObject.javaClass, any()) } returns null

            objectMockk(Import.Companion).use {
                every { Import.save(any()) } just Runs

                if (cfgObject !is CfgTenant) {
                    mockRetrieveTenant(service)
                }

                val count = importConfigurationObjects(objects, service)
                count shouldBe 2
                verify(exactly = 2) { Import.save(ofType(cfgObject.javaClass.kotlin)) }
            }
        }
    }
}
