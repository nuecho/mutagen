package com.nuecho.genesys.cli.models

import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.nuecho.genesys.cli.commands.config.ConfigMocks
import com.nuecho.genesys.cli.commands.config.export.ExportFormat
import com.nuecho.genesys.cli.commands.config.import.Import
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.models.configuration.DN
import com.nuecho.genesys.cli.models.configuration.PhysicalSwitch
import com.nuecho.genesys.cli.models.configuration.Script
import com.nuecho.genesys.cli.models.configuration.Switch
import com.nuecho.genesys.cli.models.configuration.Tenant
import com.nuecho.genesys.cli.models.configuration.reference.PhysicalSwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.services.ServiceMocks
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

private const val PHYSICAL_SWITCH_NAME = "physSwitch"
private const val PHYSICAL_SWITCH_TYPE = "CFGNortelMeridian"

class ImportPlanTest {

    @Test
    fun `dependency cycles should be detected and abort the import`() {
        val service = ServiceMocks.mockConfService()

        val scriptName = "script1"
        val tenantName = "tenant1"

        val tenantReference = TenantReference(tenantName)
        val scriptReference = ScriptReference(scriptName, tenantReference)

        val configuration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            tenants = listOf(Tenant(tenantName, defaultCapacityRule = scriptReference)),
            scripts = listOf(Script(tenant = tenantReference, name = scriptName, type = "voiceFile")),
            physicalSwitches = listOf(PhysicalSwitch(PHYSICAL_SWITCH_NAME, PHYSICAL_SWITCH_TYPE))
        )

        every { service.retrieveObject(CfgTenant::class.java, any()) } returns null
        every { service.retrieveObject(CfgScript::class.java, any()) } returns null
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

        assertThrows(ConfigurationObjectCycleException::class.java) {
            Import.importConfiguration(configuration, service, true)
        }
    }

    @Test
    fun `missing configuration object dependencies should be detected and abort the import`() {
        val configuration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            scripts = listOf(Script(tenant = TenantReference("tenant"), name = "script1", type = "voiceFile")),
            physicalSwitches = listOf(PhysicalSwitch(PHYSICAL_SWITCH_NAME, type = "nortelMeridian"))
        )

        val service = ServiceMocks.mockConfService()
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns null
        every { service.retrieveObject(CfgScript::class.java, any()) } returns null
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

        // XXX we should test at the plan.checkMissingDependencies level
        assertThrows(UnresolvedConfigurationObjectReferenceException::class.java) {
            Import.importConfiguration(configuration, service, true)
        }
    }

    @Test
    fun `missing mandatory properties in new configuration objects should be detected and abort the import`() {
        val configuration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            physicalSwitches = listOf(PhysicalSwitch(PHYSICAL_SWITCH_NAME))
        )

        val service = ServiceMocks.mockConfService()
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

        // XXX we should test at the plan.checkMissingProperties level
        assertThrows(MandatoryPropertiesNotSetException::class.java) {
            Import.importConfiguration(configuration, service, true)
        }
    }

    @Test
    fun `object creation should be performed in the correct order according to the dependencies between objects`() {
        val tenantName = "tenant"
        val switchName = "switch"

        val physicalSwitchReference = PhysicalSwitchReference(PHYSICAL_SWITCH_NAME)
        val tenantReference = TenantReference(tenantName)
        val switchReference = SwitchReference(tenant = tenantReference, name = switchName)

        val physicalSwitch = PhysicalSwitch(PHYSICAL_SWITCH_NAME, PHYSICAL_SWITCH_TYPE)
        val tenant = Tenant(tenantName)
        val switch = Switch(tenant = tenantReference, name = switchName, physicalSwitch = physicalSwitchReference)
        val dn = DN(tenant = tenantReference, type = CfgDNType.CFGCP.toShortName(), number = "123", switch = switchReference, routeType = "CFGDefault")

        val configuration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            physicalSwitches = listOf(physicalSwitch),
            tenants = listOf(tenant),
            switches = listOf(switch),
            dns = listOf(dn)
        )

        val service = ServiceMocks.mockConfService()
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns null
        every { service.retrieveObject(CfgScript::class.java, any()) } returns null
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

        val operations = ImportPlan.applyOperationOrder(configuration, ImportPlan.toOperations(service, configuration))

        assertThat(operations, hasSize(4))

        // physicalSwitch and tenant can legally be in any order
        operations.subList(0, 2).containsAll(listOf(ImportPlanOperation(service, physicalSwitch), ImportPlanOperation(service, tenant)))
        assertThat(operations[2].configurationObject as Switch, equalTo(switch))
        assertThat(operations[3].configurationObject as DN, equalTo(dn))
    }
}
