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

import com.genesyslab.platform.applicationblocks.com.ConfService
import com.genesyslab.platform.applicationblocks.com.ConfServiceFactory
import com.genesyslab.platform.applicationblocks.com.ConfigServerException
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.configuration.protocol.ConfServerProtocol
import com.nuecho.mutagen.cli.Logging
import com.nuecho.mutagen.cli.preferences.environment.Environment
import com.nuecho.mutagen.cli.services.GenesysServices.createConfServerProtocol

class ConfService private constructor(
    private val protocol: ConfServerProtocol,
    private val confService: ConfService
) : Service, IConfService by confService {
    constructor(
        environment: Environment,
        checkCertificate: Boolean
    ) : this(createConfServerProtocol(environment, checkCertificate))

    internal constructor(protocol: ConfServerProtocol) : this(
        protocol,
        ConfServiceFactory.createConfService(protocol, true) as ConfService
    )

    internal val isDisposed: Boolean
        get() = confService.isDisposed

    override fun open() {
        Logging.info {
            "Connecting to Config Server [${protocol.userName}@${protocol.endpoint.host}:${protocol.endpoint.port}]"
        }

        try {
            protocol.open()
        } catch (exception: Exception) {
            throw ConfigServerException(
                "Error while connecting to Config Server [${protocol.endpoint.host}:${protocol.endpoint.port}]."
            ).initCause(exception)
        }

        Logging.debug { "Connected to Config Server." }
    }

    override fun close() {
        Logging.debug { "Disconnecting from Config Server" }

        try {
            if (protocol.state !== ChannelState.Closed) {
                protocol.close()
            }

            if (!isDisposed) {
                ConfServiceFactory.releaseConfService(confService)
            }
        } catch (exception: Exception) {
            throw ConfigServerException("Error while disconnecting from Config Server.").initCause(exception)
        }

        Logging.debug { "Disconnected from Config Server." }
    }
}
