package com.nuecho.genesys.cli.commands.config.import.operation

import com.genesyslab.platform.applicationblocks.com.objects.CfgAccessGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgRole
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.genesyslab.platform.configuration.protocol.types.CfgRouteType.CFGIDDD
import com.genesyslab.platform.configuration.protocol.types.CfgSwitchType.CFGFujitsu
import com.genesyslab.platform.configuration.protocol.types.CfgTargetType.CFGNoTarget
import com.nuecho.genesys.cli.CliOutputCaptureWrapper.captureOutput
import com.nuecho.genesys.cli.TestResources
import com.nuecho.genesys.cli.commands.config.ConfigMocks
import com.nuecho.genesys.cli.commands.config.export.ExportFormat
import com.nuecho.genesys.cli.commands.config.import.operation.ImportPlan.Companion.findMissingDependencies
import com.nuecho.genesys.cli.commands.config.import.operation.ImportPlan.Companion.findMissingProperties
import com.nuecho.genesys.cli.commands.config.import.operation.ImportPlan.Companion.toOperations
import com.nuecho.genesys.cli.models.configuration.AccessGroup
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.models.configuration.DISPLAY_NAME
import com.nuecho.genesys.cli.models.configuration.DN
import com.nuecho.genesys.cli.models.configuration.Enumerator
import com.nuecho.genesys.cli.models.configuration.PhysicalSwitch
import com.nuecho.genesys.cli.models.configuration.Role
import com.nuecho.genesys.cli.models.configuration.Script
import com.nuecho.genesys.cli.models.configuration.Switch
import com.nuecho.genesys.cli.models.configuration.SwitchAccessCode
import com.nuecho.genesys.cli.models.configuration.TYPE
import com.nuecho.genesys.cli.models.configuration.Tenant
import com.nuecho.genesys.cli.models.configuration.reference.ApplicationReference
import com.nuecho.genesys.cli.models.configuration.reference.PhysicalSwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.just
import io.mockk.objectMockk
import io.mockk.runs
import io.mockk.staticMockk
import io.mockk.use
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@SuppressWarnings("LargeClass")
class ImportPlanTest {

    @Test
    fun `findMissingDependencies should detect missing mandatory properties`() {
        val existingPhysicalSwitch = PhysicalSwitchReference("existingPhysicalSwitch")
        val existingTenant = TenantReference("existingTenant")

        val missingTenant = TenantReference("missingTenant")
        val missingApplication = ApplicationReference("missingTServer")
        val missingSwitch1 = SwitchReference("missingSwitch1", existingTenant)
        val missingSwitch2 = SwitchReference("missingSwitch2", existingTenant)

        val script = Script(tenant = missingTenant, name = "script", type = "voiceFile")
        val physicalSwitch = PhysicalSwitch(existingPhysicalSwitch.primaryKey)
        val switch = Switch(
            tenant = existingTenant,
            name = "skill",
            physicalSwitch = existingPhysicalSwitch,
            tServer = missingApplication,
            switchAccessCodes = listOf(
                SwitchAccessCode(
                    switch = missingSwitch1,
                    routeType = CFGIDDD.toShortName(),
                    targetType = CFGNoTarget.toShortName()
                ),
                SwitchAccessCode(
                    switch = missingSwitch2,
                    routeType = CFGIDDD.toShortName(),
                    targetType = CFGNoTarget.toShortName()
                )
            )
        )

        val configuration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            scripts = listOf(script),
            physicalSwitches = listOf(physicalSwitch),
            switches = listOf(switch)
        )

        val service = mockConfService()
        staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
            val cfgPhysicalSwitch = CfgPhysicalSwitch(service)
            val cfgTenant = CfgTenant(service)
            every { service.retrieveObject(existingPhysicalSwitch) } returns cfgPhysicalSwitch
            every { service.retrieveObject(existingTenant) } returns cfgTenant
            every { service.retrieveObject(missingTenant) } returns null
            every { service.retrieveObject(missingApplication) } returns null
            every { service.retrieveObject(missingSwitch1) } returns null
            every { service.retrieveObject(missingSwitch2) } returns null

            val missingDependencies = findMissingDependencies(service, configuration)

            assertThat(
                missingDependencies,
                containsInAnyOrder(
                    MissingDependencies(script, setOf(missingTenant)),
                    MissingDependencies(switch, setOf(missingApplication, missingSwitch1, missingSwitch2))
                )
            )
        }
    }

    @Test
    fun `findMissingProperties should detect missing mandatory properties in new configuration objects`() {
        val physicalSwitch = PhysicalSwitch("physSwitch")
        val enumerator = Enumerator(name = "enumerator", tenant = TenantReference("tenant"))

        val configuration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            physicalSwitches = listOf(physicalSwitch),
            enumerators = listOf(enumerator)
        )

        val service = mockConfService()
        val cfgTenant = CfgTenant(service)
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null
        every { service.retrieveObject(CfgEnumerator::class.java, any()) } returns null
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant

        val missingProperties = findMissingProperties(service, toOperations(service, configuration))

        assertThat(
            missingProperties,
            containsInAnyOrder(
                MissingProperties(physicalSwitch, setOf(TYPE)),
                MissingProperties(enumerator, setOf(DISPLAY_NAME, TYPE))
            )
        )
    }

    @Test
    fun `applyOperationOrder should return operation in the correct order according to the dependencies between objects`() {
        val tenantName = "tenant"
        val switchName = "switch"

        val physicalSwitchReference = PhysicalSwitchReference("physSwitch")
        val tenantReference = TenantReference(tenantName)
        val switchReference = SwitchReference(tenant = tenantReference, name = switchName)

        val physicalSwitch = PhysicalSwitch(physicalSwitchReference.primaryKey, CFGFujitsu.toShortName())
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

        val service = mockConfService()
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns null
        every { service.retrieveObject(CfgScript::class.java, any()) } returns null
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

        val operations = ImportPlan.applyOperationOrder(service, configuration)

        assertThat(operations, hasSize(4))

        // physicalSwitch and tenant can legally be in any order, but the result is stable
        assertThat(operations[0].configurationObject as PhysicalSwitch, equalTo(physicalSwitch))
        assertThat(operations[1].configurationObject as Tenant, equalTo(tenant))
        assertThat(operations[2].configurationObject as Switch, equalTo(switch))
        assertThat(operations[3].configurationObject as DN, equalTo(dn))
    }

    @Test
    fun `applyOperationOrder should break dependency cycles`() {
        val service = mockConfService()

        val scriptName = "script1"
        val tenantName = "tenant1"

        val tenantReference = TenantReference(tenantName)
        val scriptReference = ScriptReference(scriptName, tenantReference)

        val tenant = Tenant(tenantName, defaultCapacityRule = scriptReference)
        val script = Script(tenant = tenantReference, name = scriptName, type = "voiceFile")
        val physicalSwitch = PhysicalSwitch("physSwitch", CFGFujitsu.toShortName())

        val configuration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            tenants = listOf(tenant),
            scripts = listOf(script),
            physicalSwitches = listOf(physicalSwitch)
        )

        every { service.retrieveObject(CfgTenant::class.java, any()) } returns null
        every { service.retrieveObject(CfgScript::class.java, any()) } returns null
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

        val operations = ImportPlan.applyOperationOrder(service, configuration)

        assertThat(operations, hasSize(4))
        assertThat(operations[0].configurationObject as PhysicalSwitch, equalTo(physicalSwitch))
        assertThat(operations[1].configurationObject as Tenant, equalTo(tenant.cloneBare()))
        assertThat(operations[2].configurationObject as Script, equalTo(script))
        assertThat(operations[3].configurationObject as Tenant, equalTo(tenant))
    }

    @Test
    fun `applyOperationOrder should ignore dependency cycles on UPDATE operations`() {
        val service = mockConfService()

        val scriptName = "script1"
        val tenantName = "tenant1"

        val tenantReference = TenantReference(tenantName)
        val scriptReference = ScriptReference(scriptName, tenantReference)

        val tenant = Tenant(tenantName, defaultCapacityRule = scriptReference)
        val script = Script(tenant = tenantReference, name = scriptName, type = "voiceFile")

        val configuration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            tenants = listOf(tenant),
            scripts = listOf(script)
        )

        val cfgTenant = mockCfgTenant("tenantName")

        every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant
        every { service.retrieveObject(CfgScript::class.java, any()) } returns null

        val operations = ImportPlan.applyOperationOrder(service, configuration)

        assertThat(operations, hasSize(2))
        assertThat(operations[0].configurationObject as Script, equalTo(script))
        assertThat(operations[1].configurationObject as Tenant, equalTo(tenant))
    }

    @Test
    fun `printPlan should print operations in details`() {
        val service = mockConfService()

        val tenantReference = TenantReference("existingTenant")
        val operations = listOf(
            CreateOperation(
                PhysicalSwitch(name = "physSwitch", type = CFGFujitsu.toShortName()),
                service
            ),
            UpdateOperation(
                Switch(name = "switch", tenant = tenantReference, dnRange = "12-15"),
                CfgSwitch(service),
                service
            ),
            UpdateReferenceOperation(
                Tenant(
                    name = tenantReference.primaryKey,
                    defaultCapacityRule = ScriptReference("script", tenantReference)
                ),
                service
            )
        )

        val (_, output) = captureOutput { ImportPlan.printPlan(operations) }

        val expected = TestResources.getTestResource("commands/config/import/operation/printPlan.txt").readText()
        assertThat(output, equalTo(expected))
    }

    @Test
    fun `operation type should be CREATE when remote object does not exist`() {
        val service = mockConfService()

        every { service.retrieveObject(CfgTenant::class.java, any()) } returns null
        every { service.retrieveObject(CfgAccessGroup::class.java, any()) } returns null

        val operation = ImportPlan.toOperation(
            service,
            AccessGroup(tenant = ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE, name = "name")
        )

        assertThat(operation.type, CoreMatchers.equalTo(ImportOperationType.CREATE))
    }

    @Test
    fun `operation type should be UPDATE when remote object exists and is modified`() {
        val service = mockConfService()

        val mockCfgTenant = mockCfgTenant(DEFAULT_TENANT)

        val remoteCfgObject = CfgRole(service).also {
            it.tenantDBID = ConfigurationObjectMocks.DEFAULT_TENANT_DBID
            it.name = "name"
        }

        val modifiedObject = Role(
            tenant = ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE,
            name = "name",
            description = "updated description"
        )

        objectMockk(ImportOperation).use {
            every { service.retrieveObject(CfgTenant::class.java, any()) } returns mockCfgTenant
            every { service.retrieveObject(CfgRole::class.java, any()) } returns remoteCfgObject
            every { ImportOperation.save(any()) } just runs

            val operation = ImportPlan.toOperation(service, modifiedObject)

            assertThat(operation.type, CoreMatchers.equalTo(ImportOperationType.UPDATE))
        }
    }

    @Disabled
    @Test
    fun `operation type should be SKIP when remote object exists and is not modified`() {
        val service = mockConfService()

        val mockCfgTenant = mockCfgTenant(DEFAULT_TENANT)

        val remoteCfgObject = CfgRole(service).also {
            it.tenantDBID = DEFAULT_TENANT_DBID
            it.name = "name"
        }

        val unmodifiedObject = Role(
            tenant = DEFAULT_TENANT_REFERENCE,
            name = "name"
        )

        objectMockk(ImportOperation).use {
            every { service.retrieveObject(CfgTenant::class.java, any()) } returns mockCfgTenant
            every { service.retrieveObject(CfgRole::class.java, any()) } returns remoteCfgObject

            val operation = ImportPlan.toOperation(service, unmodifiedObject)

            assertThat(operation.type, CoreMatchers.equalTo(ImportOperationType.SKIP))
        }
    }
}