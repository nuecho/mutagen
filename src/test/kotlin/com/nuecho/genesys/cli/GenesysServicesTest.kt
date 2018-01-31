package com.nuecho.genesys.cli

import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.configuration.protocol.ConfServerProtocol
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.nuecho.genesys.cli.GenesysServices.createConfigurationService
import com.nuecho.genesys.cli.GenesysServices.releaseConfigurationService
import com.nuecho.genesys.cli.preferences.Environment
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

class GenesysServicesTest : StringSpec() {
    init {
        "createConfigurationService should create a valid IConfService" {
            val host = "host.nuecho.com"
            val port = 1234
            val user = "user"
            val password = "password"
            val environment = Environment(
                host = host,
                port = port,
                user = user,
                password = password)

            val applicationType = CfgAppType.CFGAdvisors
            val configurationService = createConfigurationService(environment, applicationType)
            val protocol = configurationService.protocol as ConfServerProtocol
            val endpoint = protocol.endpoint

            endpoint.host shouldBe host
            endpoint.port shouldBe port

            protocol.userName shouldBe user
            protocol.userPassword shouldBe password
            protocol.clientApplicationType shouldBe applicationType.asInteger()
            protocol.state shouldBe ChannelState.Closed

            releaseConfigurationService(configurationService)
            protocol.state shouldBe ChannelState.Closed
        }
    }
}