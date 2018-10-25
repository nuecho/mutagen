/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nuecho.mutagen.cli.commands.config.import.operation

import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgPhysicalSwitch
import com.nuecho.mutagen.cli.models.configuration.PhysicalSwitch
import com.nuecho.mutagen.cli.services.ServiceMocks.mockConfService
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.objectMockk
import io.mockk.runs
import io.mockk.use
import io.mockk.verify
import org.junit.jupiter.api.Test

class UpdateOperationTest : ImportOperationTest(
    UpdateOperation(
        PhysicalSwitch(name = "physSwitch"),
        mockCfgPhysicalSwitch("physSwitch"),
        mockConfService()
    )
) {
    @Test
    fun `apply() should update and save cfgObject`() {
        val physicalSwitch = mockk<PhysicalSwitch>()
        val cfgPhysicalSwitch = mockCfgPhysicalSwitch("physSwitch")
        val service = mockConfService()

        objectMockk(ImportOperation).use {
            every { physicalSwitch.updateCfgObject(service, cfgPhysicalSwitch) } returns cfgPhysicalSwitch
            every { ImportOperation.save(any()) } just runs

            UpdateOperation(physicalSwitch, cfgPhysicalSwitch, service).apply()

            verify(exactly = 1) { physicalSwitch.updateCfgObject(service, cfgPhysicalSwitch) }
            verify(exactly = 1) { ImportOperation.save(any()) }
        }
    }
}
