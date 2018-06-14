package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.nuecho.genesys.cli.commands.config.import.Import.Companion.importConfigurationObject
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.models.configuration.DN
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.getObjectDbid
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
    val DN1 = DN(
        tenant = DEFAULT_TENANT_REFERENCE,
        number = "1",
        switch = SwitchReference("aswitch", DEFAULT_TENANT_REFERENCE),
        type = CfgDNType.CFGACDPosition.toShortName()
    )
    val DN2 = DN(
        tenant = DEFAULT_TENANT_REFERENCE,
        number = "2",
        switch = SwitchReference("aswitch", DEFAULT_TENANT_REFERENCE),
        type = CfgDNType.CFGCP.toShortName()
    )

    init {
        "importing an existing DN should do nothing" {
            val service = mockConfService()

            val tenant = mockCfgTenant(ConfigurationObjectMocks.DEFAULT_TENANT)

            val cfgSwitch = mockk<CfgSwitch>().also {
                every { it.name } returns "aswitch"
                every { it.objectDbid } returns 101
            }

            val cfgDn = CfgDN(service).apply {
                number = "1"
                switchDBID = 101
                type = CfgDNType.CFGACDPosition
            }

            every { service.retrieveObject(CfgTenant::class.java, any()) } returns tenant
            every { service.retrieveObject(CfgDN::class.java, any()) } returns cfgDn
            every { service.retrieveObject(CfgSwitch::class.java, any()) } returns cfgSwitch

            objectMockk(Import.Companion).use {
                val hasImportedObject = importConfigurationObject(DN1, service)
                hasImportedObject shouldBe false
                verify(exactly = 0) { Import.save(any()) }
            }
        }

        "importing a new DN should try to save it" {
            val switch = mockk<CfgSwitch>().apply {
                every { objectDbid } returns 101
                every { name } returns "aswitch"
            }

            objectMockk(Import.Companion).use {
                staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
                    val service = mockConfService()
                    every { service.retrieveObject(CfgDN::class.java, any()) } returns null
                    every { service.retrieveObject(CfgSwitch::class.java, any()) } returns switch
                    every { service.getObjectDbid(ofType(TenantReference::class)) } returns DEFAULT_TENANT_DBID
                    every { Import.save(any()) } just Runs

                    val hasImportedObject = importConfigurationObject(DN1, service)
                    hasImportedObject shouldBe true
                    verify(exactly = 1) { Import.save(ofType(CfgDN::class)) }
                }
            }
        }
    }
}
