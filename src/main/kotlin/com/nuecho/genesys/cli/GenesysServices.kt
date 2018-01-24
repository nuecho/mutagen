package com.nuecho.genesys.cli

import com.genesyslab.platform.applicationblocks.com.ConfServiceFactory
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.commons.connection.ConnectionException
import com.genesyslab.platform.commons.connection.configuration.PropertyConfiguration
import com.genesyslab.platform.commons.connection.tls.KeyManagerHelper
import com.genesyslab.platform.commons.connection.tls.SSLContextHelper
import com.genesyslab.platform.commons.connection.tls.TrustManagerHelper
import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.commons.protocol.ProtocolException
import com.genesyslab.platform.configuration.protocol.ConfServerProtocol
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import java.net.URI
import javax.net.ssl.SSLContext

object GenesysServices {
    public const val DEFAULT_APPLICATION_NAME = "default"
    public const val DEFAULT_SERVER_PORT = 2020
    public const val DEFAULT_SERVER_TIMEOUT = 20
    public const val DEFAULT_CLIENT_TIMEOUT = 10

    /**
     * Create a Genesys configuration service.
     *
     * @param username The user name
     * @param password The password
     * @param applicationName The application name
     * @param applicationType The application type
     * @param endpoint The endpoint
     * @return IConfService
     * @throws ConnectionException When a connection problem occurs
     */
    @Throws(ConnectionException::class)
    fun createConfigurationService(
        username: String,
        password: String,
        applicationName: String,
        applicationType: CfgAppType,
        endpoint: Endpoint): IConfService {

        val protocol = ConfServerProtocol(endpoint)
        protocol.clientApplicationType = applicationType.ordinal()
        protocol.userName = username
        protocol.userPassword = password
        protocol.clientName = applicationName

        try {
            val configurationService = ConfServiceFactory.createConfService(protocol)
            configurationService.protocol.open()
            return configurationService
        } catch (exception: Exception) {
            throw ConnectionException("Can't create IConfService", exception)
        }
    }

    /**
     * Release a Genesys configuration service.
     *
     * @param service The service
     * @throws InterruptedException in case the close operation was interrupted.
     * @throws ProtocolException if there is any problem related to the connection close
     */
    @Throws(InterruptedException::class, ProtocolException::class)
    fun releaseConfigurationService(service: IConfService?) {
        if (service != null) {
            val protocol = service.protocol

            if (protocol.state !== ChannelState.Closed) {
                protocol.close()
            }

            ConfServiceFactory.releaseConfService(service)
        }
    }

    /**
     * Create an endpoint.
     *
     * @param primaryUri The primary URI
     * @param id The id
     * @param addpClientTimeout The client timeout
     * @param addpServerTimeout The server timeout
     * @param tlsEnabled Enable/disable TLS
     * @return Endpoint
     * @throws ConnectionException When a connection problem occurs
     */
    @Throws(ConnectionException::class)
    fun createEndPoint(
        primaryUri: URI,
        id: String,
        addpClientTimeout: Int,
        addpServerTimeout: Int,
        tlsEnabled: Boolean): Endpoint {

        val propertyConfiguration = PropertyConfiguration()
        propertyConfiguration.isUseAddp = true
        propertyConfiguration.addpClientTimeout = addpClientTimeout
        propertyConfiguration.addpServerTimeout = addpServerTimeout
        propertyConfiguration.isTLSEnabled = tlsEnabled

        return Endpoint(
            id,
            primaryUri.host,
            primaryUri.port,
            propertyConfiguration,
            tlsEnabled,
            createSslContext(),
            null)
    }

    @Throws(ConnectionException::class)
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