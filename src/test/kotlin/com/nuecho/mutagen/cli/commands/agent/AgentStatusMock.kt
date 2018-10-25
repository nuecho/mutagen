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

package com.nuecho.mutagen.cli.commands.agent

import com.genesyslab.platform.reporting.protocol.statserver.AgentStatus
import com.genesyslab.platform.reporting.protocol.statserver.DnActions
import com.genesyslab.platform.reporting.protocol.statserver.DnStatus
import com.genesyslab.platform.reporting.protocol.statserver.DnStatusesCollection
import com.genesyslab.platform.reporting.protocol.statserver.PlaceStatus
import io.mockk.every
import io.mockk.mockk

internal fun mockAgentStatus(
    agentId: String = "test",
    placeId: String = "testPlace",
    switchId: String = "testSwitch",
    dnId: String = "1234",
    status: DnActions = DnActions.WaitForNextCall
): AgentStatus {
    val dnStatus = mockk<DnStatus>()
    every { dnStatus.switchId } returns switchId
    every { dnStatus.dnId } returns dnId

    val dnStatusesCollection = mockk<DnStatusesCollection>()
    every { dnStatusesCollection.count } returns 1
    every { dnStatusesCollection.getItem(any()) } returns dnStatus

    val placeStatus = mockk<PlaceStatus>()
    every { placeStatus.dnStatuses } returns dnStatusesCollection
    every { placeStatus.placeId } returns placeId

    val agentStatus = mockk<AgentStatus>()
    every { agentStatus.agentId } returns agentId
    every { agentStatus.status } returns status.asInteger()
    every { agentStatus.place } returns placeStatus

    return agentStatus
}
