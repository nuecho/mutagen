package com.nuecho.genesys.cli.agent

import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.voice.protocol.TServerProtocol
import com.genesyslab.platform.voice.protocol.tserver.events.EventAgentLogout
import com.genesyslab.platform.voice.protocol.tserver.events.EventError
import com.genesyslab.platform.voice.protocol.tserver.events.EventRegistered
import com.genesyslab.platform.voice.protocol.tserver.events.EventUnregistered
import com.genesyslab.platform.voice.protocol.tserver.requests.agent.RequestAgentLogout
import com.genesyslab.platform.voice.protocol.tserver.requests.dn.RequestRegisterAddress
import com.genesyslab.platform.voice.protocol.tserver.requests.dn.RequestUnregisterAddress
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.StringSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify

class TServiceTest : StringSpec() {
    init {
        "opening TService should open protocol" {
            val protocol = mockTServerProtocol()

            TService(protocol).open()

            verify { protocol.open() }
        }

        "closing an closed TService should not fail" {
            val protocol = mockTServerProtocol()
            every { protocol.state } returns ChannelState.Closed

            TService(protocol).close()

            verify(inverse = true) { protocol.close() }
        }

        "closing a connected TService should close protocol" {
            val protocol = mockTServerProtocol()

            TService(protocol).close()

            verify { protocol.close() }
        }

        "logoutAdress should work" {
            TService(mockTServerProtocol()).logoutAdress("123")
            // TODO we should verify the mock has been called, but it doesn't work
        }

        "logoutAdress should throw if registerAddress fail" {
            val protocol = mockTServerProtocol()
            val service = TService(protocol)

            every { protocol.request(ofType(RequestRegisterAddress::class)) } returns EventError.create()

            shouldThrow<TServiceException> {
                service.logoutAdress("123")
            }
        }

        "logoutAdress should throw if agentLogout fail" {
            val protocol = mockTServerProtocol()
            val service = TService(protocol)

            every { protocol.request(ofType(RequestAgentLogout::class)) } returns EventError.create()

            shouldThrow<TServiceException> {
                service.logoutAdress("123")
            }
        }

        "logoutAdress should throw if unregisterAddress fail" {
            val protocol = mockTServerProtocol()
            val service = TService(protocol)

            every { protocol.request(ofType(RequestUnregisterAddress::class)) } returns EventError.create()

            shouldThrow<TServiceException> {
                service.logoutAdress("123")
            }
        }
    }
}

private fun mockTServerProtocol(): TServerProtocol {
    val protocol = mockk<TServerProtocol>()
    every { protocol.endpoint } returns Endpoint("testEndpoint", 1234)
    every { protocol.state } returns ChannelState.Opened
    every { protocol.open() } just Runs
    every { protocol.close() } just Runs
    every { protocol.request(ofType(RequestRegisterAddress::class)) } returns EventRegistered.create()
    every { protocol.request(ofType(RequestAgentLogout::class)) } returns EventAgentLogout.create()
    every { protocol.request(ofType(RequestUnregisterAddress::class)) } returns EventUnregistered.create()
    return protocol
}
