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
import com.nuecho.mutagen.cli.Logging.debug
import com.nuecho.mutagen.cli.Logging.info

class TService internal constructor(private val protocol: TServerProtocol) : Service, Protocol by protocol {
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

    fun logoutAddresses(dns: List<String>) = dns.forEach { logoutAddress(it) }

    fun logoutAddress(dn: String) {
        registerAddress(dn)

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

    private fun registerAddress(dn: String) {
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
