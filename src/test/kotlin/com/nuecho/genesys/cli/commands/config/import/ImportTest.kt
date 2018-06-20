package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGCP
import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import com.nuecho.genesys.cli.commands.config.ConfigMocks.mockMetadata
import com.nuecho.genesys.cli.commands.config.export.ExportFormat.JSON
import com.nuecho.genesys.cli.commands.config.import.Import.Companion.confirm
import com.nuecho.genesys.cli.commands.config.import.Import.Companion.extractTopologicalSequence
import com.nuecho.genesys.cli.commands.config.import.Import.Companion.importConfiguration
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.models.configuration.ConfigurationBuilder
import com.nuecho.genesys.cli.models.configuration.DN
import com.nuecho.genesys.cli.models.configuration.PhysicalSwitch
import com.nuecho.genesys.cli.models.configuration.Script
import com.nuecho.genesys.cli.models.configuration.Switch
import com.nuecho.genesys.cli.models.configuration.Tenant
import com.nuecho.genesys.cli.models.configuration.reference.PhysicalSwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.containsAll
import io.kotlintest.matchers.haveSize
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.matchers.startWith
import io.kotlintest.specs.StringSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.objectMockk
import io.mockk.staticMockk
import io.mockk.use
import io.mockk.verify

private const val USAGE_PREFIX = "Usage: import [-?]"

class ImportTest : StringSpec() {
    init {
        "executing Import with -h argument should print usage" {
            val output = execute("config", "import", "-h")
            output should startWith(USAGE_PREFIX)
        }

        "importing empty configuration should do nothing" {
            val metadata = mockMetadata(JSON)
            val configuration = ConfigurationBuilder().build(metadata)
            val service = mockConfService()

            objectMockk(Import.Companion).use {
                importConfiguration(configuration, service, true)
                verify(exactly = 0) { Import.save(any()) }
            }
        }

        "refusing to apply plan should abort the import" {
            val service = mockConfService()

            val configuration = Configuration(
                __metadata__ = mockMetadata(JSON),
                physicalSwitches = listOf(PhysicalSwitch("physSwitch"))
            )

            staticMockk("kotlin.io.ConsoleKt").use {
                every { readLine() } returns "n"
                every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

                objectMockk(Import.Companion).use {
                    importConfiguration(configuration, service, false)
                    verify(exactly = 0) { Import.save(any()) }
                }
            }
        }

        "accepting to apply plan should perform the import" {
            val service = mockConfService()

            val configuration = Configuration(
                __metadata__ = mockMetadata(JSON),
                physicalSwitches = listOf(PhysicalSwitch("physSwitch"))
            )

            staticMockk("kotlin.io.ConsoleKt").use {
                every { readLine() } returns "y"
                every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

                objectMockk(Import.Companion).use {
                    every { Import.save(any()) } just Runs

                    importConfiguration(configuration, service, false)
                    verify(exactly = 1) { Import.save(any()) }
                }
            }
        }

        "dependency cycles should be detected and abort the import" {
            val service = mockConfService()

            val scriptName = "script"
            val tenantName = "tenant"

            val tenantReference = TenantReference(tenantName)
            val scriptReference = ScriptReference(scriptName, tenantReference)

            val configuration = Configuration(
                __metadata__ = mockMetadata(JSON),
                tenants = listOf(Tenant(tenantName, defaultCapacityRule = scriptReference)),
                scripts = listOf(Script(tenant = tenantReference, name = scriptName)),
                physicalSwitches = listOf(PhysicalSwitch("physSwitch"))
            )

            shouldThrow<ConfigurationObjectCycleException> {
                importConfiguration(configuration, service, true)
            }
        }

        "missing configuration objects should be detected and abort the import" {
            val configuration = Configuration(
                __metadata__ = mockMetadata(JSON),
                scripts = listOf(Script(tenant = TenantReference("tenant"), name = "script1")),
                physicalSwitches = listOf(PhysicalSwitch("physSwitch"))
            )

            val service = mockConfService()
            every { service.retrieveObject(CfgTenant::class.java, any()) } returns null

            shouldThrow<UnresolvedConfigurationObjectReferenceException> {
                importConfiguration(configuration, service, true)
            }
        }

        "object creation should be performed in the correct order according to the dependencies between objects" {
            val physicalSwitchName = "physSwitch"
            val tenantName = "tenant"
            val switchName = "switch"

            val physicalSwitchReference = PhysicalSwitchReference(physicalSwitchName)
            val tenantReference = TenantReference(tenantName)
            val switchReference = SwitchReference(tenant = tenantReference, name = switchName)

            val physicalSwitch = PhysicalSwitch(physicalSwitchName)
            val tenant = Tenant(tenantName)
            val switch = Switch(tenant = tenantReference, name = switchName, physicalSwitch = physicalSwitchReference)
            val dn = DN(tenant = tenantReference, type = CFGCP.toShortName(), number = "123", switch = switchReference)

            val configuration = Configuration(
                __metadata__ = mockMetadata(JSON),
                physicalSwitches = listOf(physicalSwitch),
                tenants = listOf(tenant),
                switches = listOf(switch),
                dns = listOf(dn)
            )

            val sortedConfigurationObjects = extractTopologicalSequence(configuration, mockConfService())

            sortedConfigurationObjects should haveSize(4)

            // physicalSwitch and tenant can legally be in any order
            sortedConfigurationObjects.subList(0, 2) should containsAll(physicalSwitch, tenant)
            sortedConfigurationObjects[2] shouldBe switch
            sortedConfigurationObjects[3] shouldBe dn
        }

        "confirm should return true when user enters y" {
            staticMockk("kotlin.io.ConsoleKt").use {
                every { readLine() } returns "y" andThen "Y"
                confirm() shouldBe true
                confirm() shouldBe true
            }
        }

        "confirm should return false when user enters n" {
            staticMockk("kotlin.io.ConsoleKt").use {
                every { readLine() } returns "n" andThen "N"
                confirm() shouldBe false
                confirm() shouldBe false
            }
        }

        "confirm should keep prompting until user enters a valid answer" {
            staticMockk("kotlin.io.ConsoleKt").use {
                every { readLine() } returns "q" andThen "t" andThen "Y"
                confirm() shouldBe true
            }
        }
    }
}
