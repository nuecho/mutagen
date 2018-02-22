package com.nuecho.genesys.cli.services

import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.reporting.protocol.StatServerProtocol
import com.genesyslab.platform.reporting.protocol.statserver.requests.RequestCloseStatistic
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class StatServiceTest : StringSpec() {
    init {
        "opening StatService should open protocol" {
            val protocol = mockStatServerProtocol()
            every { protocol.state } returns ChannelState.Closed

            StatService(protocol).open()

            verify { protocol.open() }
        }

        "closing a closed StatService should not fail" {
            val protocol = mockStatServerProtocol()
            every { protocol.state } returns ChannelState.Closed

            StatService(protocol).close()

            verify(inverse = true) { protocol.close() }
        }

        "closing a connected StatService should close protocol" {
            val protocol = mockStatServerProtocol()
            every { protocol.state } returns ChannelState.Opened

            StatService(protocol).close()

            verify { protocol.close() }
        }

        "calling closeStatistic should send a RequestCloseStatistic" {
            val protocol = mockStatServerProtocol()

            StatService(protocol).closeStatistic(1)

            verify { protocol.send(ofType(RequestCloseStatistic::class)) }
        }
    }
}

private fun mockStatServerProtocol(): StatServerProtocol = mockk<StatServerProtocol>(relaxed = true)
