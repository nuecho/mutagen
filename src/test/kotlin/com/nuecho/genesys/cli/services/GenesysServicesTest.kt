package com.nuecho.genesys.cli.services

import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.GenesysServices.createConfServerProtocol
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
                rawPassword = password
            )

            val protocol = createConfServerProtocol(environment)
            val endpoint = protocol.endpoint

            endpoint.host shouldBe host
            endpoint.port shouldBe port

            protocol.userName shouldBe user
            protocol.userPassword shouldBe password
            protocol.clientApplicationType shouldBe CfgAppType.CFGSCE.asInteger()
            protocol.state shouldBe ChannelState.Closed
        }
    }
}
