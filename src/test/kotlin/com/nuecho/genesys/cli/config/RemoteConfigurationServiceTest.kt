package com.nuecho.genesys.cli.config

import com.genesyslab.platform.applicationblocks.com.CfgQuery
import com.genesyslab.platform.applicationblocks.com.ConfigException
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.nuecho.genesys.cli.GenesysServices
import com.nuecho.genesys.cli.preferences.Environment
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.StringSpec

class RemoteConfigurationServiceTest : StringSpec() {
    init {
        "querying an unconnected RemoteConfigurationService should throw an exception" {
            val service = createRemoteConfigurationService()

            shouldThrow<ConfigException> {
                service.retrieveMultipleObjects(ICfgObject::class.java, CfgQuery())
            }
        }

        "releasing an unconnected RemoteConfigurationService should not fail" {
            val service = createRemoteConfigurationService()
            service.release()
        }
    }

    fun createRemoteConfigurationService(): RemoteConfigurationService {
        val environment = Environment(host = "test", user = "test", password = "test")
        val service = GenesysServices.createConfigurationService(environment, CfgAppType.CFGConfigServer)
        return RemoteConfigurationService(service)
    }
}