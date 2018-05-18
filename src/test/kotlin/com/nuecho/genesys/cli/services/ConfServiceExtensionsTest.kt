package com.nuecho.genesys.cli.services

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgQuery
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.nuecho.genesys.cli.preferences.environment.Environment
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.staticMockk
import io.mockk.use
import io.mockk.verify

class ConfServiceExtensionsTest : StringSpec() {
    init {
        val service = mockConfService()
        val type = slot<Class<out ICfgObject>>()
        val query = slot<ICfgQuery>()

        every {
            service.retrieveObject(capture(type), capture(query))
        } returns null

        "default tenant should fail on no tenant" {
            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
                every { service.tenants } returns listOf()
                shouldThrow<IllegalStateException> {
                    service.defaultTenantDbid
                }
            }
        }
        "default tenant should fail on multi tenant" {
            var tenants = listOf(CfgTenant(service), CfgTenant(service))

            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
                every { service.tenants } returns tenants

                shouldThrow<IllegalStateException> {
                    service.defaultTenantDbid
                }
            }
        }
        "default tenant should return single tenant" {
            val tenant = mockk<CfgTenant>()
            every { tenant.getDBID() } returns 1

            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
                every { service.tenants } returns listOf(tenant)
                val actual = service.defaultTenantDbid
                actual shouldBe 1
            }
        }
        "tenant retrieval should be cached" {
            val tenant = mockk<CfgTenant>()
            every { tenant.getDBID() } returns 1

            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
                every { service.retrieveTenants() } returns listOf(tenant)
                service.defaultTenantDbid
                service.defaultTenantDbid
                verify(exactly = 1) { service.retrieveTenants() }
            }
        }
    }

    private fun mockConfService() = spyk(ConfService(Environment(host = "test", user = "test", rawPassword = "test")))
}
