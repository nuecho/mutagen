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

package com.nuecho.mutagen.cli.services

import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.configuration.protocol.ConfServerProtocol
import com.genesyslab.platform.configuration.protocol.confserver.ConfServerProtocolFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
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
        assertThat(service.isDisposed, `is`(true))
    }

    @Test
    fun `disconnecting an already disconnected and disposed ConfService should not fail`() {
        every { protocol.state } returns ChannelState.Opened

        service.close()
        service.close()

        verify { protocol.close() }
        assertThat(service.isDisposed, `is`(true))
    }
}

private fun mockConfServerProtocol(): ConfServerProtocol {
    val protocol = mockk<ConfServerProtocol>(relaxed = true)
    every { protocol.protocolDescription } returns ConfServerProtocolFactory.PROTOCOL_DESCRIPTION
    return protocol
}
