package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import com.nuecho.genesys.cli.commands.config.import.Import.Companion.applyTenant
import com.nuecho.genesys.cli.commands.config.import.Import.Companion.importPersons
import com.nuecho.genesys.cli.models.configuration.ConfigurationBuilder
import com.nuecho.genesys.cli.models.configuration.Person
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.defaultTenantDbid
import io.kotlintest.matchers.should
import io.kotlintest.matchers.startWith
import io.kotlintest.specs.StringSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.objectMockk
import io.mockk.spyk
import io.mockk.staticMockk
import io.mockk.use
import io.mockk.verify

private const val USAGE_PREFIX = "Usage: import [-?]"
private const val DEFAULT_TENANT_DBID = 1

class ImportTest : StringSpec() {
    init {
        "executing Import with -h argument should print usage" {
            val output = execute("config", "import", "-h")
            output should startWith(USAGE_PREFIX)
        }

        "importing empty configuration should do nothing" {
            val configuration = ConfigurationBuilder().build()
            val service = mockConfService()

            objectMockk(Import.Companion).use {
                Import.importConfiguration(configuration, service)
                verify(exactly = 0) { Import.Companion.save(any()) }
            }
        }

        "importing an existing person should do nothing" {
            val service = mockConfService()
            val cfgPerson = CfgPerson(service)

            every { service.retrieveObject(CfgPerson::class.java, any()) } returns cfgPerson

            val persons = listOf(Person("employeeId", "userName"))

            objectMockk(Import.Companion).use {
                Import.importPersons(persons, service)
                verify(exactly = 0) { Import.Companion.save(any()) }
            }
        }

        "importing a new person should try to save it" {

            val service = mockConfService()
            every { service.retrieveObject(CfgPerson::class.java, any()) } returns null

            objectMockk(Import.Companion).use {

                staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {

                    every { service.defaultTenantDbid } returns 1
                    every { Import.Companion.save(any()) } just Runs

                    importPersons(listOf(Person("employeeId", "userName")), service)
                    verify(exactly = 1) { Import.Companion.save(ofType(CfgPerson::class)) }
                }
            }
        }

        "applying tenant should set default tenant" {

            val service = mockConfService()

            val cfgObject = mockk<CfgObject>()
            every { cfgObject.configurationService } returns service
            every { cfgObject.setProperty("tenantDBID", DEFAULT_TENANT_DBID) } just Runs

            objectMockk(Import.Companion).use {
                staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {

                    every { service.defaultTenantDbid } returns DEFAULT_TENANT_DBID

                    applyTenant(cfgObject)

                    verify(exactly = 1) { cfgObject.setProperty("tenantDBID", DEFAULT_TENANT_DBID) }
                }
            }
        }
    }

    private fun mockConfService() = spyk(ConfService(Environment(host = "test", user = "test", rawPassword = "test")))
}
