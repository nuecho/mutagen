package com.nuecho.genesys.cli.commands.agent

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
