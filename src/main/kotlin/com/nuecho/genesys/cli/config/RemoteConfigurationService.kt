package com.nuecho.genesys.cli.config

import com.genesyslab.platform.applicationblocks.com.CfgQuery
import com.genesyslab.platform.applicationblocks.com.ConfServiceFactory
import com.genesyslab.platform.applicationblocks.com.ConfigServerException
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.nuecho.genesys.cli.GenesysServices
import com.nuecho.genesys.cli.preferences.environment.Environment
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class RemoteConfigurationService(private val environment: Environment) : ConfigurationService {
    private val configurationService: IConfService

    init {
        configurationService = GenesysServices.createConfigurationService(environment, CfgAppType.CFGSCE)
    }

    override fun connect() {
        logger.info { "Connecting to Config Server [${environment.user}@${environment.host}:${environment.port}]" }

        try {
            configurationService.protocol.open()
        } catch (exception: Exception) {
            throw ConfigServerException(
                "Error while connecting to Config Server [${environment.host}:${environment.port}]."
            ).initCause(exception)
        }

        logger.debug { "Connected to Config Server." }
    }

    override fun <T : ICfgObject> retrieveMultipleObjects(objectType: Class<T>, query: CfgQuery): Collection<T> {
        return configurationService.retrieveMultipleObjects(objectType, query) ?: emptyList()
    }

    override fun disconnect() {
        logger.debug { "Disconnecting from Config Server" }

        try {
            val protocol = configurationService.protocol

            if (protocol.state !== ChannelState.Closed) {
                protocol.close()
            }

            ConfServiceFactory.releaseConfService(configurationService)
        } catch (exception: Exception) {
            throw ConfigServerException("Error while disconnecting from Config Server.").initCause(exception)
        }

        logger.debug { "Disconnected from Config Server." }
    }
}
