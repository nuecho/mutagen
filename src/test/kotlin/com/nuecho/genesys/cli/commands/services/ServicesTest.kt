package com.nuecho.genesys.cli.commands.services

import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.nuecho.genesys.cli.CliOutputCaptureWrapper
import com.nuecho.genesys.cli.core.defaultJsonGenerator
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.getDefaultEndpoint
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.mockk
import io.mockk.staticMockk
import io.mockk.use
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.net.URI

private const val USAGE_PREFIX = "Usage: services [-?]"

private const val VERSION = "8.5"
private const val TYPE = "tserver"
private val ENDPOINT_URI = URI.create("tcp://foo.example.com:1234")
private const val SOCKET_TIMEOUT = 200

class ServicesTest {
    @Test
    fun `executing services with -h argument should print usage`() {
        val output = CliOutputCaptureWrapper.execute("services", "-h")

        assertTrue(output.startsWith(USAGE_PREFIX))
    }

    @Test
    fun `given an application name should return a short name`() {

        val application = mockk<CfgApplication>()
        every { application.type } returns CfgAppType.CFGTServer

        val shortName = application.type.toShortName()
        assertEquals(shortName, TYPE)
    }

    @Test
    fun `given a server application should be accepted`() {

        val application = mockk<CfgApplication>()
        every { application.isServer } returns CfgFlag.CFGTrue

        staticMockk("com.nuecho.genesys.cli.GenesysExtensionsKt").use {
            every { application.getDefaultEndpoint() } returns Endpoint(ENDPOINT_URI)

            val accepted = Services.accept(application)
            assertTrue(accepted)
        }
    }

    @Test
    fun `given a non-server application should not be accepted`() {

        val application = mockk<CfgApplication>()

        every { application.isServer } returns CfgFlag.CFGFalse

        val accepted = Services.accept(application)
        assertFalse(accepted)
    }

    @Test
    fun `given a non-matching server application type filter should not be accepted`() {

        val application = mockk<CfgApplication>()
        every { application.isServer } returns CfgFlag.CFGTrue
        every { application.type } returns CfgAppType.CFGTServer

        staticMockk("com.nuecho.genesys.cli.GenesysExtensionsKt").use {
            every { application.getDefaultEndpoint() } returns Endpoint(ENDPOINT_URI)
            every { application.type.toShortName() } returns "tserver"

            val accepted = Services.accept(application, listOf("NonMatchingType"))
            assertFalse(accepted)
        }
    }

    @Test
    fun `given a matching server application type filter should  be accepted`() {

        val application = mockk<CfgApplication>()
        every { application.isServer } returns CfgFlag.CFGTrue
        every { application.type } returns CfgAppType.CFGTServer

        staticMockk("com.nuecho.genesys.cli.GenesysExtensionsKt").use {
            every { application.getDefaultEndpoint() } returns Endpoint(ENDPOINT_URI)
            every { application.type.toShortName() } returns "tserver"

            val accepted = Services.accept(application, listOf(TYPE))
            assertTrue(accepted)
        }
    }

    @Test
    fun `given an server application should return a service object`() {

        val application = mockk<CfgApplication>()
        every { application.isServer } returns CfgFlag.CFGTrue
        every { application.type } returns CfgAppType.CFGTServer
        every { application.version } returns VERSION
        every { application.name } returns "foo"
        every { application.isPrimary } returns CfgFlag.CFGTrue

        staticMockk("com.nuecho.genesys.cli.GenesysExtensionsKt").use {
            every { application.getDefaultEndpoint()!!.uri } returns ENDPOINT_URI
            every { application.type.toShortName() } returns "tserver"

            val service = Services.toServiceDefinition(application)

            assertEquals(ServiceDefinition("foo", TYPE, VERSION, ENDPOINT_URI, true), service)
        }
    }

    @Test
    fun `given a list of server application should generate proper JSON output`() {

        val application = mockk<CfgApplication>()
        every { application.isServer } returns CfgFlag.CFGTrue
        every { application.type } returns CfgAppType.CFGTServer
        every { application.version } returns VERSION
        every { application.name } returns "foo"
        every { application.isPrimary } returns CfgFlag.CFGTrue

        staticMockk("com.nuecho.genesys.cli.GenesysExtensionsKt").use {
            every { application.getDefaultEndpoint()!!.uri } returns ENDPOINT_URI
            every { application.type.toShortName() } returns "tserver"

            val output = ByteArrayOutputStream()

            Services().writeServices(listOf(application), defaultJsonGenerator(output))

            val result = defaultJsonObjectMapper().readTree(String(output.toByteArray()))

            assertEquals(defaultJsonObjectMapper().readTree(javaClass.getResourceAsStream("services.json")), result)
        }
    }

    @Test
    fun `given an unreachable endpoint should return status as DOWN`() {
        val result = Services.status(Endpoint(ENDPOINT_URI), true, SOCKET_TIMEOUT)
        assertEquals(Status.DOWN, result)
    }
}
