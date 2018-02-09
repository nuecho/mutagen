package com.nuecho.genesys.cli.config

import com.genesyslab.platform.applicationblocks.com.CfgQuery
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgQuery

class TestConfigurationService(private val configuration: Map<ICfgQuery, Collection<ICfgObject>>) :
    ConfigurationService {

    override fun <T : ICfgObject> retrieveMultipleObjects(objectType: Class<T>, query: CfgQuery): Collection<T> {
        val result = configuration[query] ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        return result as Collection<T>
    }

    override fun release() {}
}
