package com.nuecho.genesys.cli.services

import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.GenesysServices.createConfServerProtocol
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class GenesysServicesTest {
    val host = "host.nuecho.com"
    val port = 1234
    val user = "user"
    val password = "password"

    @Test
    fun `createConfServerProtocol should create a valid IConfService`() {
        val environment = Environment(
            host = host,
            port = port,
            user = user,
            rawPassword = password
        )

        val protocol = createConfServerProtocol(environment, true)
        val endpoint = protocol.endpoint

        assertThat(endpoint.host, equalTo(host))
        assertThat(endpoint.port, equalTo(port))
        assertThat(endpoint.configuration.getOption("string-attributes-encoding"), equalTo("utf-8"))

        assertThat(protocol.userName, equalTo(user))
        assertThat(protocol.clientApplicationType, equalTo(CfgAppType.CFGSCE.asInteger()))
        assertThat(protocol.state, equalTo(ChannelState.Closed))
    }

    @Test
    fun `createConfServerProtocol should throw an exception if an invalid encoding is specified`() {
        val environment = Environment(
            host = host,
            port = port,
            user = user,
            rawPassword = password,
            encoding = "invalidEncoding"
        )

        assertThrows(InvalidEncodingException::class.java) { createConfServerProtocol(environment, true) }
    }

    @Test
    fun `createConfServerProtocol should set encoding to specified valid encoding`() {
        val environment = Environment(
            host = host,
            port = port,
            user = user,
            rawPassword = password,
            encoding = "gb2312"
        )

        val protocol = createConfServerProtocol(environment, true)
        val endpoint = protocol.endpoint

        assertThat(endpoint.configuration.getOption("string-attributes-encoding"), equalTo("gb2312"))
    }
}
