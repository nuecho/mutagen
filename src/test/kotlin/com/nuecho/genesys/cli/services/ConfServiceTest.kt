package com.nuecho.genesys.cli.services

import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.configuration.protocol.ConfServerProtocol
import com.genesyslab.platform.configuration.protocol.confserver.ConfServerProtocolFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
class ConfServiceTest {
    private val protocol = mockConfServerProtocol()
    private val service = ConfService(protocol)

    @Test
    fun `connecting a ConfService should open its protocol`() {
        every { protocol.state } returns ChannelState.Closed
        service.open()

        verify { protocol.open() }
    }

    @Test
    fun `disconnecting a ConfService should close its protocol and dispose its delegate`() {
        every { protocol.state } returns ChannelState.Opened
        service.close()

        verify { protocol.close() }
        assertTrue(service.isDisposed)
    }

    @Test
    fun `disconnecting an already disconnected and disposed ConfService should not fail`() {
        every { protocol.state } returns ChannelState.Opened

        service.close()
        service.close()

        verify { protocol.close() }
        assertTrue(service.isDisposed)
    }
}

private fun mockConfServerProtocol(): ConfServerProtocol {
    val protocol = mockk<ConfServerProtocol>(relaxed = true)
    every { protocol.protocolDescription } returns ConfServerProtocolFactory.PROTOCOL_DESCRIPTION
    return protocol
}
