package com.nuecho.genesys.cli

import com.genesyslab.platform.applicationblocks.com.ConfServiceFactory
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.commons.connection.ConnectionException
import com.genesyslab.platform.commons.connection.configuration.PropertyConfiguration
import com.genesyslab.platform.commons.connection.tls.KeyManagerHelper
import com.genesyslab.platform.commons.connection.tls.SSLContextHelper
import com.genesyslab.platform.commons.connection.tls.TrustManagerHelper
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.configuration.protocol.ConfServerProtocol
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.nuecho.genesys.cli.preferences.Environment
import java.util.UUID
import javax.net.ssl.SSLContext

object GenesysServices {
    const val DEFAULT_SERVER_PORT = 2020
    const val DEFAULT_USE_TLS = false
    const val DEFAULT_APPLICATION_NAME = "default"
    private const val DEFAULT_SERVER_TIMEOUT = 20
    private const val DEFAULT_CLIENT_TIMEOUT = 10

    /**
     * Create a Genesys configuration service.
     *
     * @param environment The environment
     * @param applicationType The application type
     * @param endpoint The endpoint
     * @return IConfService
     * @throws ConnectionException When a connection problem occurs
     */
    fun createConfigurationService(
        environment: Environment,
        applicationType: CfgAppType
    ): IConfService {

        var endpoint = createEndpoint(environment)
        val protocol = ConfServerProtocol(endpoint)
        protocol.clientApplicationType = applicationType.ordinal()
        protocol.userName = environment.user
        protocol.userPassword = environment.password
        protocol.clientName = environment.application

        return ConfServiceFactory.createConfService(protocol)
    }

    /**
     * Create an endpoint.
     *
     * @param endpointName The endpoint name
     * @param environment The environment
     * @return Endpoint
     * @throws ConnectionException When a connection problem occurs
     */
    private fun createEndpoint(environment: Environment): Endpoint {
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
        try {
            val keyManager = KeyManagerHelper.createEmptyKeyManager()
            val trustManager = TrustManagerHelper.createDefaultTrustManager()
            return SSLContextHelper.createSSLContext(keyManager, trustManager)
        } catch (exception: Exception) {
            throw ConnectionException("Security configuration error.", exception)
        }
    }
}
