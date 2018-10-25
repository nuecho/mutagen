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

import com.genesyslab.platform.commons.connection.configuration.PropertyConfiguration
import com.genesyslab.platform.commons.connection.tls.KeyManagerHelper
import com.genesyslab.platform.commons.connection.tls.SSLContextHelper
import com.genesyslab.platform.commons.connection.tls.TrustManagerHelper
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.configuration.protocol.ConfServerProtocol
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.nuecho.mutagen.cli.preferences.environment.Environment
import java.util.UUID
import javax.net.ssl.SSLContext

object GenesysServices {
    const val DEFAULT_SERVER_PORT = 2020
    const val DEFAULT_USE_TLS = false
    const val DEFAULT_APPLICATION_NAME = "default"
    const val DEFAULT_CLIENT_TIMEOUT = 10
    private const val DEFAULT_SERVER_TIMEOUT = 20
    private val VALID_ENCODINGS = listOf(
        "utf-8", "utf-16", "ascii", "iso-8859-1",
        "iso-8859-2", "iso-8859-3", "iso-8859-4", "iso-8859-5", "iso-8859-6", "iso-8859-7", "iso-8859-8",
        "iso-8859-9", "ebcdic-cp-us", "ibm1140", "gb2312", "big5", "koi8-r", "shift_jis", "euc-kr"
    )

    fun createConfServerProtocol(environment: Environment, checkCertificate: Boolean): ConfServerProtocol {
        val endpoint = GenesysServices.createConfServerEndpoint(environment, checkCertificate)
        val protocol = ConfServerProtocol(endpoint)
        protocol.clientApplicationType = CfgAppType.CFGSCE.ordinal()
        protocol.userName = environment.user
        protocol.userPassword = environment.password!!.value
        protocol.clientName = environment.application

        return protocol
    }

    private fun createConfServerEndpoint(environment: Environment, checkCertificate: Boolean): Endpoint {
        if (!VALID_ENCODINGS.contains(environment.encoding.toLowerCase())) {
            throw InvalidEncodingException(
                "Encoding '${environment.encoding}' specified in " +
                        "environments.yml is not supported by confServer connection."
            )
        }

        val propertyConfiguration = PropertyConfiguration()
        propertyConfiguration.isUseAddp = true
        propertyConfiguration.addpClientTimeout = DEFAULT_CLIENT_TIMEOUT
        propertyConfiguration.addpServerTimeout = DEFAULT_SERVER_TIMEOUT
        propertyConfiguration.isTLSEnabled = environment.tls
        propertyConfiguration.stringsEncoding = environment.encoding

        val endpointName = UUID.randomUUID().toString()

        return Endpoint(
            endpointName,
            environment.host,
            environment.port,
            propertyConfiguration,
            environment.tls,
            createSslContext(checkCertificate),
            null
        )
    }

    private fun createSslContext(checkCertificate: Boolean): SSLContext {
        val keyManager = KeyManagerHelper.createEmptyKeyManager()
        val trustManager =
            if (checkCertificate) TrustManagerHelper.createDefaultTrustManager()
            else TrustManagerHelper.createTrustEveryoneTrustManager()
        return SSLContextHelper.createSSLContext(keyManager, trustManager)
    }
}

class InvalidEncodingException(message: String) : Exception(message)
