package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.nuecho.genesys.cli.commands.config.import.Import.Companion.importConfigurationObjects
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.services.defaultTenantDbid
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.objectMockk
import io.mockk.staticMockk
import io.mockk.use
import io.mockk.verify

abstract class ImportObjectSpec(cfgObject: CfgObject, objects: List<ConfigurationObject>) : StringSpec() {
    init {
        val type = objects.first()::class.simpleName!!.toLowerCase()

        assert(objects.size > 1)

        "importing an existing $type should do nothing" {
            val service = cfgObject.configurationService

            every { service.retrieveObject(cfgObject.javaClass, any()) } returns cfgObject

            objectMockk(Import.Companion).use {
                val count = importConfigurationObjects(objects, service)
                count shouldBe 0
                verify(exactly = 0) { Import.Companion.save(any()) }
            }
        }

        "importing a new $type should try to save it" {

            val service = cfgObject.configurationService
            every { service.retrieveObject(cfgObject.javaClass, any()) } returns null

            objectMockk(Import.Companion).use {

                staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {

                    every { service.defaultTenantDbid } returns 1
                    every { Import.Companion.save(any()) } just Runs

                    val count = importConfigurationObjects(objects.subList(0, 1), service)
                    count shouldBe 1
                    verify(exactly = 1) { Import.Companion.save(ofType(cfgObject.javaClass.kotlin)) }
                }
            }
        }

        "importing multiple $type should try to save all of them" {

            val service = cfgObject.configurationService
            every { service.retrieveObject(cfgObject.javaClass, any()) } returns null

            objectMockk(Import.Companion).use {

                staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {

                    every { service.defaultTenantDbid } returns 1
                    every { Import.Companion.save(any()) } just Runs

                    val count = importConfigurationObjects(objects, service)
                    count shouldBe 2
                    verify(exactly = 2) { Import.Companion.save(ofType(cfgObject.javaClass.kotlin)) }
                }
            }
        }
    }
}
