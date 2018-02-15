package com.nuecho.genesys.cli.config

import com.genesyslab.platform.applicationblocks.com.CfgQuery
import com.genesyslab.platform.applicationblocks.com.ICfgObject

interface ConfigurationService {
    fun connect()
    fun <T : ICfgObject> retrieveMultipleObjects(objectType: Class<T>, query: CfgQuery): Collection<T>
    fun <T : ICfgObject> retrieveObject(objectType: Class<T>, query: CfgQuery): T
    fun disconnect()
}
