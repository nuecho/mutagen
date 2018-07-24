package com.nuecho.genesys.cli.commands.config.import.operation

import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPhysicalSwitch
import com.nuecho.genesys.cli.models.configuration.PhysicalSwitch
import com.nuecho.genesys.cli.models.configuration.reference.PhysicalSwitchReference
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.retrieveObject
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.objectMockk
import io.mockk.runs
import io.mockk.use
import io.mockk.verify
import org.junit.jupiter.api.Test

class UpdateReferenceOperationTest : ImportOperationTest(
    UpdateReferenceOperation(
        PhysicalSwitch(name = "physSwitch"),
        mockConfService()
    )
) {
    @Test
    fun `apply() should update and save cfgObject`() {
        val physicalSwitchReference = PhysicalSwitchReference("physSwitch")
        val physicalSwitch = mockk<PhysicalSwitch>()
        val cfgPhysicalSwitch = mockCfgPhysicalSwitch("physSwitch")
        val service = mockConfService()

        objectMockk(ImportOperation).use {
            every { physicalSwitch.reference } returns physicalSwitchReference
            every { service.retrieveObject(physicalSwitchReference) } returns cfgPhysicalSwitch
            every { physicalSwitch.updateCfgObject(service, cfgPhysicalSwitch) } returns cfgPhysicalSwitch
            every { ImportOperation.save(any()) } just runs

            UpdateReferenceOperation(physicalSwitch, service).apply()

            verify(exactly = 1) { service.retrieveObject(physicalSwitchReference) }
            verify(exactly = 1) { physicalSwitch.updateCfgObject(service, cfgPhysicalSwitch) }
            verify(exactly = 1) { ImportOperation.save(any()) }
        }
    }
}
