package com.nuecho.genesys.cli.services

import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.commons.protocol.Protocol
import com.genesyslab.platform.reporting.protocol.StatServerProtocol
import com.genesyslab.platform.reporting.protocol.statserver.requests.RequestCloseStatistic
import com.nuecho.genesys.cli.Logging.debug
import com.nuecho.genesys.cli.Logging.info

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
