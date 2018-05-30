package com.nuecho.genesys.cli.services

import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.GenesysServices.createConfServerProtocol
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

class GenesysServicesTest : StringSpec() {
    init {
        val host = "host.nuecho.com"
        val port = 1234
        val user = "user"
        val password = "password"

        "createConfigurationService should create a valid IConfService" {
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
            endpoint.configuration.getOption("string-attributes-encoding") shouldBe "utf-8"

            protocol.userName shouldBe user
            protocol.userPassword shouldBe password
            protocol.clientApplicationType shouldBe CfgAppType.CFGSCE.asInteger()
            protocol.state shouldBe ChannelState.Closed
        }

        "createConfigurationService should set encoding to utf-8 if an invalid encoding is specified" {
            val environment = Environment(
                host = host,
                port = port,
                user = user,
                rawPassword = password,
                encoding = "invalidEncoding"
            )

            val protocol = createConfServerProtocol(environment)
            val endpoint = protocol.endpoint

            endpoint.configuration.getOption("string-attributes-encoding") shouldBe "utf-8"
        }

        "createConfigurationService should set encoding to valid specified encoding" {
            val environment = Environment(
                host = host,
                port = port,
                user = user,
                rawPassword = password,
                encoding = "gb2312"
            )

            val protocol = createConfServerProtocol(environment)
            val endpoint = protocol.endpoint

            endpoint.configuration.getOption("string-attributes-encoding") shouldBe "gb2312"
        }
    }
}
