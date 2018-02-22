package com.nuecho.genesys.cli.agent

import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.reporting.protocol.StatServerProtocol
import io.kotlintest.specs.StringSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify

class StatServiceTest : StringSpec() {
    init {
        "opening StatService should open protocol" {
            val protocol = mockStatServerProtocol()

            StatService(protocol).open()

            verify { protocol.open() }
        }

        "closing an closed StatService should not fail" {
            val protocol = mockStatServerProtocol()
            every { protocol.state } returns ChannelState.Closed

            StatService(protocol).close()

            verify(inverse = true) { protocol.close() }
        }

        "closing a connected StatService should close protocol" {
            val protocol = mockStatServerProtocol()

            StatService(protocol).close()

            verify { protocol.close() }
        }
    }
}

private fun mockStatServerProtocol(): StatServerProtocol {
    val protocol = mockk<StatServerProtocol>()
    every { protocol.endpoint } returns Endpoint("testEndpoint", 1234)
    every { protocol.state } returns ChannelState.Opened
    every { protocol.open() } just Runs
    every { protocol.close() } just Runs
    return protocol
}
