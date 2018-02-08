package com.nuecho.genesys.cli.config

import com.genesyslab.platform.applicationblocks.com.CfgQuery
import com.genesyslab.platform.applicationblocks.com.ConfServiceFactory
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.commons.protocol.ChannelState

class RemoteConfigurationService(private val service: IConfService) : ConfigurationService {
    override fun <T : ICfgObject> retrieveMultipleObjects(objectType: Class<T>, query: CfgQuery): Collection<T> {
        return service.retrieveMultipleObjects(objectType, query) ?: emptyList()
    }

    override fun release() {
        val protocol = service.protocol

        if (protocol.state !== ChannelState.Closed) {
            protocol.close()
        }

        ConfServiceFactory.releaseConfService(service)
    }
}
