package com.nuecho.genesys.cli.commands.config.validate

import com.genesyslab.platform.applicationblocks.com.objects.CfgAppPrototype
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.configuration.protocol.types.CfgAppType.CFGAdvisors
import com.genesyslab.platform.configuration.protocol.types.CfgAppType.CFGAgentDesktop
import com.genesyslab.platform.configuration.protocol.types.CfgRouteType
import com.genesyslab.platform.configuration.protocol.types.CfgScriptType.CFGSchedule
import com.genesyslab.platform.configuration.protocol.types.CfgSwitchType.CFGCiscoCM
import com.genesyslab.platform.configuration.protocol.types.CfgTargetType
import com.nuecho.genesys.cli.commands.config.ConfigMocks
import com.nuecho.genesys.cli.commands.config.MissingDependencies
import com.nuecho.genesys.cli.commands.config.MissingProperties
import com.nuecho.genesys.cli.commands.config.UnchangeableProperties
import com.nuecho.genesys.cli.commands.config.ValidationException
import com.nuecho.genesys.cli.commands.config.Validator
import com.nuecho.genesys.cli.commands.config.export.ExportFormat
import com.nuecho.genesys.cli.models.configuration.AppPrototype
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.models.configuration.DISPLAY_NAME
import com.nuecho.genesys.cli.models.configuration.Enumerator
import com.nuecho.genesys.cli.models.configuration.PhysicalSwitch
import com.nuecho.genesys.cli.models.configuration.Script
import com.nuecho.genesys.cli.models.configuration.Switch
import com.nuecho.genesys.cli.models.configuration.SwitchAccessCode
import com.nuecho.genesys.cli.models.configuration.TYPE
import com.nuecho.genesys.cli.models.configuration.VERSION
import com.nuecho.genesys.cli.models.configuration.reference.ApplicationReference
import com.nuecho.genesys.cli.models.configuration.reference.PhysicalSwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.DEFAULT_NAME
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveAppPrototype
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveScript
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.staticMockk
import io.mockk.use
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

private val script = Script(tenant = TenantReference(DEFAULT_NAME), name = DEFAULT_NAME, type = "voiceFile")
private val appPrototype = AppPrototype(name = "name", type = CFGAdvisors.toShortName(), version = "1.2.3")

class ValidatorTest {

    @Test
    fun `missing configuration object dependencies should be detected and throw ValidationException`() {
        val configuration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            scripts = listOf(Script(tenant = TenantReference("tenant"), name = "script1", type = "voiceFile")),
            physicalSwitches = listOf(PhysicalSwitch("physSwitch", type = "nortelMeridian"))
        )

        val service = ServiceMocks.mockConfService()
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns null
        every { service.retrieveObject(CfgScript::class.java, any()) } returns null
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

        assertThrows(ValidationException::class.java) {
            Validator(configuration, service).validateConfiguration()
        }
    }

    @Test
    fun `missing mandatory properties should be detected and throw ValidationException`() {
        val configuration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            physicalSwitches = listOf(PhysicalSwitch("physSwitch"))
        )

        val service = ServiceMocks.mockConfService()
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

        assertThrows(ValidationException::class.java) {
            Validator(configuration, service).validateConfiguration()
        }
    }

    @Test
    fun `unchangeable configuration object properties should be detected and throw ValidationException`() {
        val invalidConfiguration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            scripts = listOf(script)
        )

        val service = ServiceMocks.mockConfService()
        mockRetrieveTenant(service)
        mockRetrieveScript(service, type = CFGSchedule)

        println(Validator(invalidConfiguration, service).findValidationErrors())

        assertThrows(ValidationException::class.java) {
            Validator(invalidConfiguration, service).validateConfiguration()
        }
    }

    @Test
    fun `findMissingDependencies should detect missing dependencies`() {
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
                    routeType = CfgRouteType.CFGIDDD.toShortName(),
                    targetType = CfgTargetType.CFGNoTarget.toShortName()
                ),
                SwitchAccessCode(
                    switch = missingSwitch2,
                    routeType = CfgRouteType.CFGIDDD.toShortName(),
                    targetType = CfgTargetType.CFGNoTarget.toShortName()
                )
            )
        )

        val configuration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            scripts = listOf(script),
            physicalSwitches = listOf(physicalSwitch),
            switches = listOf(switch)
        )

        val service = ServiceMocks.mockConfService()
        staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
            val cfgPhysicalSwitch = CfgPhysicalSwitch(service)
            val cfgTenant = CfgTenant(service)
            every { service.retrieveObject(existingPhysicalSwitch) } returns cfgPhysicalSwitch
            every { service.retrieveObject(existingTenant) } returns cfgTenant
            every { service.retrieveObject(missingTenant) } returns null
            every { service.retrieveObject(missingApplication) } returns null
            every { service.retrieveObject(missingSwitch1) } returns null
            every { service.retrieveObject(missingSwitch2) } returns null

            val missingDependencies = Validator(configuration, service).findMissingDependencies()

            MatcherAssert.assertThat(
                missingDependencies,
                Matchers.containsInAnyOrder(
                    MissingDependencies(script, setOf(missingTenant)),
                    MissingDependencies(switch, setOf(missingApplication, missingSwitch1, missingSwitch2))
                )
            )
        }
    }

    @Test
    fun `findMissingProperties should detect missing mandatory properties in configuration objects`() {
        val physicalSwitch = PhysicalSwitch("physSwitch")
        val enumerator = Enumerator(name = "enumerator", tenant = TenantReference("tenant"))

        val configuration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            enumerators = listOf(enumerator),
            physicalSwitches = listOf(physicalSwitch)
        )

        val service = ServiceMocks.mockConfService()
        val cfgTenant = CfgTenant(service)
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null
        every { service.retrieveObject(CfgEnumerator::class.java, any()) } returns null
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant

        MatcherAssert.assertThat(
            Validator(configuration, service).findMissingProperties(),
            Matchers.containsInAnyOrder(
                MissingProperties(physicalSwitch, setOf(TYPE)),
                MissingProperties(enumerator, setOf(DISPLAY_NAME, TYPE))
            )
        )
    }

    @Test
    fun `findUnchangeableProperties should detect unchangeable properties in existing configuration objects`() {
        val newPhysicalSwitch = PhysicalSwitch("physSwitch", type = CFGCiscoCM.toShortName())

        val configuration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            appPrototypes = listOf(appPrototype),
            physicalSwitches = listOf(newPhysicalSwitch),
            scripts = listOf(script)
        )

        val service = ServiceMocks.mockConfService()
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns null
        every { service.retrieveObject(CfgAppPrototype::class.java, any()) } returns null
        mockRetrieveAppPrototype(service = service, type = CFGAgentDesktop, version = "3.2.1")
        mockRetrieveScript(service, type = CFGSchedule)
        mockRetrieveTenant(service)

        MatcherAssert.assertThat(
            Validator(configuration, service).findUnchangeableProperties().toList(),
            equalTo(
                listOf(
                    UnchangeableProperties(appPrototype, setOf(TYPE, VERSION)),
                    UnchangeableProperties(script, setOf(TYPE))
                )
            )
        )
    }

    @Test
    fun `findValidationErrors should detect all error types`() {
        val missingTenant = TenantReference("missingTenant")
        val appPrototype = AppPrototype(name = "appPrototype", type = CFGAgentDesktop.toShortName(), version = "1.2.3")
        val enumerator = Enumerator(name = "enumerator", tenant = missingTenant)
        val script = Script(tenant = missingTenant, name = "script")

        val configuration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            appPrototypes = listOf(appPrototype),
            enumerators = listOf(enumerator),
            scripts = listOf(script)
        )

        val service = ServiceMocks.mockConfService()
        val cfgTenant = CfgTenant(service)
        every { service.retrieveObject(CfgScript::class.java, any()) } returns null
        every { service.retrieveObject(CfgEnumerator::class.java, any()) } returns null
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant
        every { service.retrieveObject(missingTenant) } returns null
        mockRetrieveAppPrototype(service = service, type = CFGAdvisors, version = "3.2.1")

        val expectedValidationErrors = listOf(
            MissingProperties(enumerator, setOf(DISPLAY_NAME, TYPE)),
            MissingProperties(script, setOf(TYPE)),
            MissingDependencies(enumerator, setOf(missingTenant)),
            MissingDependencies(script, setOf(missingTenant)),
            UnchangeableProperties(appPrototype, setOf(TYPE, VERSION))
        )

        MatcherAssert.assertThat(
            Validator(configuration, service).findValidationErrors(),
            equalTo(expectedValidationErrors)
        )
    }
}
