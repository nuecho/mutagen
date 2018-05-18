package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.nuecho.genesys.cli.commands.config.import.Import.Companion.importConfigurationObjects
import com.nuecho.genesys.cli.models.configuration.DN
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.defaultTenantDbid
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.objectMockk
import io.mockk.staticMockk
import io.mockk.use
import io.mockk.verify

class ImportDNTest : StringSpec() {
    val service = mockConfService()

    val DN1 = DN(
        number = "1",
        switch = SwitchReference("aswitch"),
        type = CfgDNType.CFGACDPosition.toShortName()
    )
    val DN2 = DN(
        number = "2",
        switch = SwitchReference("aswitch"),
        type = CfgDNType.CFGCP.toShortName()
    )

    init {
        "importing an existing DN should do nothing" {
            val cfgSwitch = mockk<CfgSwitch>().also {
                every { it.name } returns "aswitch"
                every { it.objectDbid } returns 101
            }

            val cfgDn = CfgDN(service).apply {
                number = "1"
                switchDBID = 101
                type = CfgDNType.CFGACDPosition
            }

            every { service.retrieveObject(CfgDN::class.java, any()) } returns cfgDn
            every { service.retrieveObject(CfgSwitch::class.java, any()) } returns cfgSwitch

            val dns = listOf(DN1)

            objectMockk(Import.Companion).use {
                val count = importConfigurationObjects(dns, service)
                count shouldBe 0
                verify(exactly = 0) { Import.Companion.save(any()) }
            }
        }

        "importing a new DN should try to save it" {
            val switch = mockk<CfgSwitch>().apply {
                every { objectDbid } returns 101
                every { name } returns "aswitch"
            }

            val service = mockConfService()
            every { service.retrieveObject(CfgDN::class.java, any()) } returns null
            every { service.retrieveObject(CfgSwitch::class.java, any()) } returns switch

            val dns = listOf(DN1)

            objectMockk(Import.Companion).use {
                staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
                    every { service.defaultTenantDbid } returns 1
                    every { Import.Companion.save(any()) } just Runs

                    val count = importConfigurationObjects(dns, service)
                    count shouldBe 1
                    verify(exactly = 1) { Import.Companion.save(ofType(CfgDN::class)) }
                }
            }
        }

        "importing multiple DNs should try to save all of them" {
            val switch = mockk<CfgSwitch>().apply {
                every { name } returns "aswitch"
                every { objectDbid } returns 101
            }

            val service = mockConfService()
            every { service.retrieveObject(CfgDN::class.java, any()) } returns null
            every { service.retrieveObject(CfgSwitch::class.java, any()) } returns switch

            val dns = listOf(DN1, DN2)

            objectMockk(Import.Companion).use {
                staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
                    every { service.defaultTenantDbid } returns 1
                    every { Import.Companion.save(any()) } just Runs

                    val count = importConfigurationObjects(dns, service)
                    count shouldBe 2
                    verify(exactly = 2) { Import.Companion.save(ofType(CfgDN::class)) }
                }
            }
        }
    }
}
