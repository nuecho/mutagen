package com.nuecho.genesys.cli.services

import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.configuration.protocol.ConfServerProtocol
import com.genesyslab.platform.configuration.protocol.confserver.ConfServerProtocolFactory
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class ConfServiceTest : StringSpec() {
    override val oneInstancePerTest = false

    private val protocol = mockConfServerProtocol()
    private val service = ConfService(protocol)

    init {
        "connecting a ConfService should open its protocol" {
            every { protocol.state } returns ChannelState.Closed
            service.open()

            verify { protocol.open() }
        }

        "disconnecting a ConfService should close its protocol and dispose its delegate" {
            every { protocol.state } returns ChannelState.Opened
            service.close()

            verify { protocol.close() }
            service.isDisposed shouldBe true
        }

        "disconnecting an already disconnected and disposed ConfService should not fail" {
            every { protocol.state } returns ChannelState.Opened

            service.close()
            service.close()

            verify { protocol.close() }
            service.isDisposed shouldBe true
        }
    }

    private fun mockConfServerProtocol(): ConfServerProtocol {
        val protocol = mockk<ConfServerProtocol>(relaxed = true)
        every { protocol.protocolDescription } returns ConfServerProtocolFactory.PROTOCOL_DESCRIPTION
        return protocol
    }
}
