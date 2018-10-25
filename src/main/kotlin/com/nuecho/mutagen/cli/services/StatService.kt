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
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.commons.protocol.Protocol
import com.genesyslab.platform.reporting.protocol.StatServerProtocol
import com.genesyslab.platform.reporting.protocol.statserver.requests.RequestCloseStatistic
import com.nuecho.mutagen.cli.Logging.debug
import com.nuecho.mutagen.cli.Logging.info

class StatService internal constructor(private val protocol: StatServerProtocol) : Service, Protocol by protocol {
    constructor(endpoint: Endpoint) : this(StatServerProtocol(endpoint))

    override fun open() {
        info { "Connecting to Stat Server ($endpoint)" }

        try {
            protocol.open()
        } catch (exception: Exception) {
            throw StatServiceException("Error while connecting to Stat Server ($endpoint).", exception)
        }

        debug { "Connected to Stat Server." }
    }

    override fun close() {
        debug { "Disconnecting from Stat Server" }

        try {
            if (state !== ChannelState.Closed) {
                protocol.close()
            }
        } catch (exception: Exception) {
            throw StatServiceException("Error while disconnecting from Stat Server.", exception)
        }

        debug { "Disconnected from Stat Server." }
    }

    fun closeStatistic(referenceId: Int) {
        val request = RequestCloseStatistic.create()
        request.setStatisticId(referenceId)
        send(request)
    }
}

class StatServiceException(message: String = "", cause: Throwable? = null) : Exception(message, cause)
