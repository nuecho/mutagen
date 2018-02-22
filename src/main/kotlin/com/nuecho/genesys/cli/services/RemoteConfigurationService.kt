package com.nuecho.genesys.cli.services

import com.genesyslab.platform.applicationblocks.com.CfgQuery
import com.genesyslab.platform.applicationblocks.com.ConfServiceFactory
import com.genesyslab.platform.applicationblocks.com.ConfigServerException
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.nuecho.genesys.cli.Logging.debug
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.preferences.environment.Environment

class RemoteConfigurationService(private val environment: Environment) : ConfigurationService {
    private val configurationService: IConfService =
        GenesysServices.createConfigurationService(environment, CfgAppType.CFGSCE)

    override fun connect() {
        info { "Connecting to Config Server [${environment.user}@${environment.host}:${environment.port}]" }

        try {
            configurationService.protocol.open()
        } catch (exception: Exception) {
            throw ConfigServerException(
                "Error while connecting to Config Server [${environment.host}:${environment.port}]."
            ).initCause(exception)
        }

        debug { "Connected to Config Server." }
    }

    override fun <T : ICfgObject> retrieveMultipleObjects(objectType: Class<T>, query: CfgQuery): Collection<T> =
        configurationService.retrieveMultipleObjects(objectType, query) ?: emptyList()

    override fun <T : ICfgObject> retrieveObject(objectType: Class<T>, query: CfgQuery): T =
        configurationService.retrieveObject(objectType, query)

    override fun disconnect() {
        debug { "Disconnecting from Config Server" }

        try {
            val protocol = configurationService.protocol

            if (protocol.state !== ChannelState.Closed) {
                protocol.close()
            }

            ConfServiceFactory.releaseConfService(configurationService)
        } catch (exception: Exception) {
            throw ConfigServerException("Error while disconnecting from Config Server.").initCause(exception)
        }

        debug { "Disconnected from Config Server." }
    }
}
