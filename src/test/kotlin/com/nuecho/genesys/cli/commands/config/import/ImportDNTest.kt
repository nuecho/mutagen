package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.nuecho.genesys.cli.commands.config.import.Import.Companion.importConfigurationObjects
import com.nuecho.genesys.cli.models.configuration.DN
import com.nuecho.genesys.cli.models.configuration.DNSwitch
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.defaultTenantDbid
import com.nuecho.genesys.cli.services.getSwitchDbid
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.objectMockk
import io.mockk.staticMockk
import io.mockk.use
import io.mockk.verify

class ImportDNTest : StringSpec() {
    init {
        "importing an existing DN should do nothing" {
            val service = mockConfService()
            val cfgDn = CfgDN(service)

            every { service.retrieveObject(CfgDN::class.java, any()) } returns cfgDn

            val dns = listOf(DN("123", DNSwitch("aswitch")))

            objectMockk(Import.Companion).use {
                val count = importConfigurationObjects(dns, service)
                count shouldBe 0
                verify(exactly = 0) { Import.Companion.save(any()) }
            }
        }

        "importing a new DN should try to save it" {

            val service = mockConfService()
            every { service.retrieveObject(CfgDN::class.java, any()) } returns null

            objectMockk(Import.Companion).use {

                staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {

                    every { service.defaultTenantDbid } returns 1
                    every { service.getSwitchDbid(any()) } returns 2
                    every { Import.Companion.save(any()) } just Runs

                    val count = importConfigurationObjects(listOf(DN("123", DNSwitch("aswitch"))), service)
                    count shouldBe 1
                    verify(exactly = 1) { Import.Companion.save(ofType(CfgDN::class)) }
                }
            }
        }

        "importing multiple DNs should try to save all of them" {

            val service = mockConfService()
            every { service.retrieveObject(CfgDN::class.java, any()) } returns null

            objectMockk(Import.Companion).use {

                staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {

                    every { service.defaultTenantDbid } returns 1
                    every { service.getSwitchDbid(any()) } returns 2
                    every { Import.Companion.save(any()) } just Runs

                    val count = importConfigurationObjects(listOf(DN("123", DNSwitch("aswitch")), DN("456", DNSwitch("aswitch"))), service)
                    count shouldBe 2
                    verify(exactly = 2) { Import.Companion.save(ofType(CfgDN::class)) }
                }
            }
        }
    }
}
