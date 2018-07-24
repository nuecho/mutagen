package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.configuration.protocol.types.CfgSwitchType.CFGFujitsu
import com.nuecho.genesys.cli.Console
import com.nuecho.genesys.cli.commands.config.ConfigMocks
import com.nuecho.genesys.cli.commands.config.export.ExportFormat
import com.nuecho.genesys.cli.commands.config.import.operation.ImportOperation
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.models.configuration.PhysicalSwitch
import com.nuecho.genesys.cli.models.configuration.Script
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.services.ServiceMocks
import com.nuecho.genesys.cli.toShortName
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.objectMockk
import io.mockk.use
import io.mockk.verify
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ImportCommandTest {

    @Test
    fun `missing configuration object dependencies should abort the import`() {
        val configuration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            scripts = listOf(Script(tenant = TenantReference("tenant"), name = "script1", type = "voiceFile")),
            physicalSwitches = listOf(PhysicalSwitch("physSwitch", type = "nortelMeridian"))
        )

        val service = ServiceMocks.mockConfService()
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns null
        every { service.retrieveObject(CfgScript::class.java, any()) } returns null
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

        assertThrows(UnresolvedConfigurationObjectReferenceException::class.java) {
            Import.importConfiguration(configuration, service, true)
        }
    }

    @Test
    fun `missing mandatory properties in new configuration objects should be detected and abort the import`() {
        val configuration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            physicalSwitches = listOf(PhysicalSwitch("physSwitch"))
        )

        val service = ServiceMocks.mockConfService()
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

        assertThrows(MandatoryPropertiesNotSetException::class.java) {
            Import.importConfiguration(configuration, service, true)
        }
    }

    @Test
    fun `importConfiguration should print plan and asks for confirmation before applying`() {
        testImportConfirmation(false, true)
    }

    @Test
    fun `importConfiguration should not import anything if the plan is not accepted`() {
        testImportConfirmation(false, false)
    }

    @Test
    fun `importConfiguration should import without confirmation in auto-confirm mode`() {
        testImportConfirmation(true)
    }

    private fun testImportConfirmation(autoConfirm: Boolean, acceptImport: Boolean = true) {
        val configuration = Configuration(
            __metadata__ = ConfigMocks.mockMetadata(ExportFormat.JSON),
            physicalSwitches = listOf(PhysicalSwitch("physSwitch", type = CFGFujitsu.toShortName()))
        )

        val service = ServiceMocks.mockConfService()
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

        objectMockk(Console).use {
            every { Console.confirm() } returns acceptImport
            objectMockk(ImportOperation.Companion).use {
                every { ImportOperation.save(any()) } just Runs

                val result = Import.importConfiguration(configuration, service, autoConfirm)

                assertThat(result, `is`(acceptImport))

                verify(exactly = if (autoConfirm) 0 else 1) { Console.confirm() }
                verify(exactly = if (acceptImport) 1 else 0) { ImportOperation.save(any()) }
            }
        }
    }
}