package com.nuecho.genesys.cli.services

import com.genesyslab.platform.commons.protocol.ChannelState
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.GenesysServices.createConfServerProtocol
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GenesysServicesTest {
    val host = "host.nuecho.com"
    val port = 1234
    val user = "user"
    val password = "password"

    @Test
    fun `createConfigurationService should create a valid IConfService`() {
        val environment = Environment(
            host = host,
            port = port,
            user = user,
            rawPassword = password
        )

        val protocol = createConfServerProtocol(environment)
        val endpoint = protocol.endpoint

        assertEquals(endpoint.host, host)
        assertEquals(endpoint.port, port)
        assertEquals(endpoint.configuration.getOption("string-attributes-encoding"), "utf-8")

        assertEquals(protocol.userName, user)
        assertEquals(protocol.userPassword, password)
        assertEquals(protocol.clientApplicationType, CfgAppType.CFGSCE.asInteger())
        assertEquals(protocol.state, ChannelState.Closed)
    }

    @Test
    fun `createConfigurationService should set encoding to utf-8 if an invalid encoding is specified`() {
        val environment = Environment(
            host = host,
            port = port,
            user = user,
            rawPassword = password,
            encoding = "invalidEncoding"
        )

        val protocol = createConfServerProtocol(environment)
        val endpoint = protocol.endpoint

        assertEquals(endpoint.configuration.getOption("string-attributes-encoding"), "utf-8")
    }

    @Test
    fun `createConfigurationService should set encoding to valid specified encoding`() {
        val environment = Environment(
            host = host,
            port = port,
            user = user,
            rawPassword = password,
            encoding = "gb2312"
        )

        val protocol = createConfServerProtocol(environment)
        val endpoint = protocol.endpoint

        assertEquals(endpoint.configuration.getOption("string-attributes-encoding"), "gb2312")
    }
}
