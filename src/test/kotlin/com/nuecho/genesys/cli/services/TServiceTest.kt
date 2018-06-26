package com.nuecho.genesys.cli.services

import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.voice.protocol.TServerProtocol
import com.genesyslab.platform.voice.protocol.tserver.events.EventAgentLogout
import com.genesyslab.platform.voice.protocol.tserver.events.EventError
import com.genesyslab.platform.voice.protocol.tserver.events.EventRegistered
import com.genesyslab.platform.voice.protocol.tserver.events.EventUnregistered
import com.genesyslab.platform.voice.protocol.tserver.requests.agent.RequestAgentLogout
import com.genesyslab.platform.voice.protocol.tserver.requests.dn.RequestRegisterAddress
import com.genesyslab.platform.voice.protocol.tserver.requests.dn.RequestUnregisterAddress
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertThrows

class TServiceTest {
    @Test
    fun `opening TService should open protocol`() {
        val protocol = mockTServerProtocol()
        every { protocol.state } returns ChannelState.Closed

        TService(protocol).open()

        verify { protocol.open() }
    }

    @Test
    fun `closing an closed TService should not fail`() {
        val protocol = mockTServerProtocol()
        every { protocol.state } returns ChannelState.Closed

        TService(protocol).close()

        verify(inverse = true) { protocol.close() }
    }

    @Test
    fun `closing a connected TService should close protocol`() {
        val protocol = mockTServerProtocol()
        every { protocol.state } returns ChannelState.Opened

        TService(protocol).close()

        verify { protocol.close() }
    }

    @Test
    fun `logoutAddress should work`() {
        TService(mockTServerProtocol()).logoutAddress("123")
        // TODO we should verify the mock has been called, but it doesn't work
    }

    @Test
    fun `logoutAddress should throw if registerAddress fail`() {
        val protocol = mockTServerProtocol()
        val service = TService(protocol)

        every { protocol.request(ofType(RequestRegisterAddress::class)) } returns EventError.create()

        assertThrows(TServiceException::class.java) {
            service.logoutAddress("123")
        }
    }

    @Test
    fun `logoutAddress should throw if agentLogout fail`() {
        val protocol = mockTServerProtocol()
        val service = TService(protocol)

        every { protocol.request(ofType(RequestAgentLogout::class)) } returns EventError.create()

        assertThrows(TServiceException::class.java) {
            service.logoutAddress("123")
        }
    }

    @Test
    fun `logoutAddress should throw if unregisterAddress fail`() {
        val protocol = mockTServerProtocol()
        val service = TService(protocol)

        every { protocol.request(ofType(RequestUnregisterAddress::class)) } returns EventError.create()

        assertThrows(TServiceException::class.java) {
            service.logoutAddress("123")
        }
    }
}

private fun mockTServerProtocol(): TServerProtocol {
    val protocol = mockk<TServerProtocol>(relaxed = true)
    every { protocol.request(ofType(RequestRegisterAddress::class)) } returns EventRegistered.create()
    every { protocol.request(ofType(RequestAgentLogout::class)) } returns EventAgentLogout.create()
    every { protocol.request(ofType(RequestUnregisterAddress::class)) } returns EventUnregistered.create()
    return protocol
}
