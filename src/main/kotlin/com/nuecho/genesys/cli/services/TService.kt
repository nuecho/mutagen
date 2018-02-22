package com.nuecho.genesys.cli.services

import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.commons.protocol.Protocol
import com.genesyslab.platform.voice.protocol.TServerProtocol
import com.genesyslab.platform.voice.protocol.tserver.AddressType
import com.genesyslab.platform.voice.protocol.tserver.ControlMode
import com.genesyslab.platform.voice.protocol.tserver.RegisterMode
import com.genesyslab.platform.voice.protocol.tserver.events.EventAgentLogout
import com.genesyslab.platform.voice.protocol.tserver.events.EventRegistered
import com.genesyslab.platform.voice.protocol.tserver.events.EventUnregistered
import com.genesyslab.platform.voice.protocol.tserver.requests.agent.RequestAgentLogout
import com.genesyslab.platform.voice.protocol.tserver.requests.dn.RequestRegisterAddress
import com.genesyslab.platform.voice.protocol.tserver.requests.dn.RequestUnregisterAddress
import com.nuecho.genesys.cli.Logging.debug
import com.nuecho.genesys.cli.Logging.info

class TService internal constructor(private val protocol: TServerProtocol) : Protocol by protocol {
    constructor(endpoint: Endpoint) : this(TServerProtocol(endpoint))

    override fun open() {
        info { "Connecting to T-Server ($endpoint)" }

        try {
            protocol.open()
        } catch (exception: Exception) {
            throw TServiceException("Error while connecting to T-Server ($endpoint).", exception)
        }

        debug { "Connected to T-Server." }
    }

    override fun close() {
        debug { "Disconnecting from T-Server" }

        try {
            if (state !== ChannelState.Closed) {
                protocol.close()
            }
        } catch (exception: Exception) {
            throw TServiceException("Error while disconnecting from T-Server.", exception)
        }

        debug { "Disconnected from T-Server." }
    }

    fun logoutAdress(dn: String) {
        registerAdress(dn)

        debug { "Logging out address ($dn) from TServer ($endpoint)" }

        val agentLogoutRequest = RequestAgentLogout.create(dn)
        val response = request(agentLogoutRequest)
        if (response.messageId() != EventAgentLogout.ID) {
            debug { "Unexpected response for RequestAgentLogout:\n" + response }
            throw TServiceException("Error logging out address ($dn). Response: (${response.messageName()}).")
        }

        debug { "Address ($dn) logged out from TServer ($endpoint)." }

        unregisterAddress(dn)
    }

    private fun registerAdress(dn: String) {
        debug { "Registering address ($dn)" }

        val request = RequestRegisterAddress.create()
        request.registerMode = RegisterMode.ModeShare
        request.thisDN = dn
        request.controlMode = ControlMode.RegisterDefault
        request.addressType = AddressType.DN

        val response = request(request)
        if (response.messageId() != EventRegistered.ID) {
            debug { "Unexpected response for RequestRegisterAddress:\n" + response }
            throw TServiceException("Error registering address ($dn). Response: (${response.messageName()}).")
        }

        debug { "Address ($dn) registered." }
    }

    private fun unregisterAddress(dn: String) {
        debug { "Unregistering address ($dn)" }

        val request = RequestUnregisterAddress.create(dn, ControlMode.RegisterDefault)

        val response = request(request)
        if (response.messageId() != EventUnregistered.ID) {
            debug { "Unexpected response for RequestUnregisterAddress:\n" + response }
            throw TServiceException("Error unregistering address ($dn). Response: (${response.messageName()}).")
        }

        debug { "Address ($dn) unregistered." }
    }
}

class TServiceException(message: String = "", cause: Throwable? = null) : Exception(message, cause)
