package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import com.nuecho.genesys.cli.commands.config.ConfigMocks.mockMetadata
import com.nuecho.genesys.cli.commands.config.export.ExportFormat.JSON
import com.nuecho.genesys.cli.commands.config.import.Import.importConfiguration
import com.nuecho.genesys.cli.models.ImportPlan
import com.nuecho.genesys.cli.models.configuration.Configuration
import com.nuecho.genesys.cli.models.configuration.ConfigurationBuilder
import com.nuecho.genesys.cli.models.configuration.PhysicalSwitch
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.objectMockk
import io.mockk.staticMockk
import io.mockk.use
import io.mockk.verify
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test

private const val USAGE_PREFIX = "Usage: import [-?]"
private const val PHYSICAL_SWITCH_NAME = "physSwitch"
private const val PHYSICAL_SWITCH_TYPE = "CFGNortelMeridian"

class ImportTest {
    @Test
    fun `executing Import with -h argument should print usage`() {
        val output = execute("config", "import", "-h")
        assertThat(output, startsWith(USAGE_PREFIX))
    }

    @Test
    fun `importing empty configuration should do nothing`() {
        val metadata = mockMetadata(JSON)
        val configuration = ConfigurationBuilder().build(metadata)
        val service = mockConfService()

        objectMockk(ImportPlan.Companion).use {
            every { ImportPlan.save(any()) } just Runs

            importConfiguration(configuration, service, true)
            verify(exactly = 0) { ImportPlan.save(any()) }
        }
    }

    @Test
    fun `refusing to apply plan should abort the import`() {
        val service = mockConfService()

        val configuration = Configuration(
            __metadata__ = mockMetadata(JSON),
            physicalSwitches = listOf(PhysicalSwitch(PHYSICAL_SWITCH_NAME, PHYSICAL_SWITCH_TYPE))
        )

        staticMockk("kotlin.io.ConsoleKt").use {
            every { readLine() } returns "n"
            every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

            objectMockk(ImportPlan.Companion).use {
                every { ImportPlan.save(any()) } just Runs

                importConfiguration(configuration, service, false)
                verify(exactly = 0) { ImportPlan.save(any()) }
            }
        }
    }

    @Test
    fun `accepting to apply plan should perform the import`() {
        val service = mockConfService()

        val configuration = Configuration(
            __metadata__ = mockMetadata(JSON),
            physicalSwitches = listOf(PhysicalSwitch(PHYSICAL_SWITCH_NAME, PHYSICAL_SWITCH_TYPE))
        )

        staticMockk("kotlin.io.ConsoleKt").use {
            every { readLine() } returns "y"
            every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } returns null

            objectMockk(ImportPlan.Companion).use {
                every { ImportPlan.save(any()) } just Runs

                importConfiguration(configuration, service, false)
                verify(exactly = 1) { ImportPlan.save(any()) }
            }
        }
    }
}
