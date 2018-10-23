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

package com.nuecho.genesys.cli.services

import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.reporting.protocol.StatServerProtocol
import com.genesyslab.platform.reporting.protocol.statserver.requests.RequestCloseStatistic
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class StatServiceTest {
    @Test
    fun `opening StatService should open protocol`() {
        val protocol = mockStatServerProtocol()
        every { protocol.state } returns ChannelState.Closed

        StatService(protocol).open()

        verify { protocol.open() }
    }

    @Test
    fun `closing a closed StatService should not fail`() {
        val protocol = mockStatServerProtocol()
        every { protocol.state } returns ChannelState.Closed

        StatService(protocol).close()

        verify(inverse = true) { protocol.close() }
    }

    @Test
    fun `closing a connected StatService should close protocol`() {
        val protocol = mockStatServerProtocol()
        every { protocol.state } returns ChannelState.Opened

        StatService(protocol).close()

        verify { protocol.close() }
    }

    @Test
    fun `calling closeStatistic should send a RequestCloseStatistic`() {
        val protocol = mockStatServerProtocol()

        StatService(protocol).closeStatistic(1)

        verify { protocol.send(ofType(RequestCloseStatistic::class)) }
    }
}

private fun mockStatServerProtocol(): StatServerProtocol = mockk<StatServerProtocol>(relaxed = true)
