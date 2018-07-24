package com.nuecho.genesys.cli.commands.config.import.operation

import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPhysicalSwitch
import com.nuecho.genesys.cli.models.configuration.PhysicalSwitch
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.objectMockk
import io.mockk.runs
import io.mockk.use
import io.mockk.verify
import org.junit.jupiter.api.Test

class CreateOperationTest : ImportOperationTest(
    CreateOperation(PhysicalSwitch(name = "physSwitch"), mockConfService())
) {
    @Test
    fun `apply() should create and save cfgObject`() {
        val physicalSwitch = mockk<PhysicalSwitch>()
        val cfgPhysicalSwitch = mockCfgPhysicalSwitch("physSwitch")
        val service = mockConfService()

        objectMockk(ImportOperation).use {
            every { physicalSwitch.createCfgObject(service) } returns cfgPhysicalSwitch
            every { ImportOperation.save(any()) } just runs

            CreateOperation(physicalSwitch, service).apply()

            verify(exactly = 1) { physicalSwitch.createCfgObject(service) }
            verify(exactly = 1) { ImportOperation.save(any()) }
        }
    }
}
