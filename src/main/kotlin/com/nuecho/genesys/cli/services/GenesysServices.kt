package com.nuecho.genesys.cli.services

import com.genesyslab.platform.commons.connection.configuration.PropertyConfiguration
import com.genesyslab.platform.commons.connection.tls.KeyManagerHelper
import com.genesyslab.platform.commons.connection.tls.SSLContextHelper
import com.genesyslab.platform.commons.connection.tls.TrustManagerHelper
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.configuration.protocol.ConfServerProtocol
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.nuecho.genesys.cli.preferences.environment.Environment
import java.util.UUID
import javax.net.ssl.SSLContext

object GenesysServices {
    const val DEFAULT_SERVER_PORT = 2020
    const val DEFAULT_USE_TLS = false
    const val DEFAULT_APPLICATION_NAME = "default"
    const val DEFAULT_CLIENT_TIMEOUT = 10
    private const val DEFAULT_SERVER_TIMEOUT = 20

    fun createConfServerProtocol(environment: Environment): ConfServerProtocol {
        val endpoint = GenesysServices.createConfServerEndpoint(environment)
        val protocol = ConfServerProtocol(endpoint)
        protocol.clientApplicationType = CfgAppType.CFGSCE.ordinal()
        protocol.userName = environment.user
        protocol.userPassword = environment.password
        protocol.clientName = environment.application

        return protocol
    }

    private fun createConfServerEndpoint(environment: Environment): Endpoint {
        val propertyConfiguration = PropertyConfiguration()
        propertyConfiguration.isUseAddp = true
        propertyConfiguration.addpClientTimeout = DEFAULT_CLIENT_TIMEOUT
        propertyConfiguration.addpServerTimeout = DEFAULT_SERVER_TIMEOUT
        propertyConfiguration.isTLSEnabled = environment.tls

        val endpointName = UUID.randomUUID().toString()

        return Endpoint(
            endpointName,
            environment.host,
            environment.port,
            propertyConfiguration,
            environment.tls,
            createSslContext(),
            null
        )
    }

    private fun createSslContext(): SSLContext {
        val keyManager = KeyManagerHelper.createEmptyKeyManager()
        val trustManager = TrustManagerHelper.createDefaultTrustManager()
        return SSLContextHelper.createSSLContext(keyManager, trustManager)
    }
}
