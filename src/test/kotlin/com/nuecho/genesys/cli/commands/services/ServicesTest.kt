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
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.startWith
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.staticMockk
import io.mockk.use
import java.io.ByteArrayOutputStream
import java.net.URI

private const val USAGE_PREFIX = "Usage: services [-?]"

private const val VERSION = "8.5"
private const val TYPE = "tserver"
private val ENDPOINT_URI = URI.create("tcp://foo.example.com:1234")
private const val SOCKET_TIMEOUT = 200

class ServicesTest : StringSpec() {
    init {
        "executing services with -h argument should print usage" {
            val output = CliOutputCaptureWrapper.execute("services", "-h")

            output should startWith(USAGE_PREFIX)
        }

        "given an application name should return a short name" {

            val application = mockk<CfgApplication>()
            every { application.type } returns CfgAppType.CFGTServer

            val shortName = application.type.toShortName()
            shortName shouldBe TYPE
        }

        "given a server application should be accepted" {

            val application = mockk<CfgApplication>()
            every { application.isServer } returns CfgFlag.CFGTrue

            staticMockk("com.nuecho.genesys.cli.GenesysExtensionsKt").use {
                every { application.getDefaultEndpoint() } returns Endpoint(ENDPOINT_URI)

                val accepted = Services.accept(application)
                accepted shouldBe true
            }
        }

        "given a non-server application should not be accepted" {

            val application = mockk<CfgApplication>()

            every { application.isServer } returns CfgFlag.CFGFalse

            val accepted = Services.accept(application)
            accepted shouldBe false
        }

        "given a non-matching server application type filter should not be accepted" {

            val application = mockk<CfgApplication>()
            every { application.isServer } returns CfgFlag.CFGTrue
            every { application.type } returns CfgAppType.CFGTServer

            staticMockk("com.nuecho.genesys.cli.GenesysExtensionsKt").use {
                every { application.getDefaultEndpoint() } returns Endpoint(ENDPOINT_URI)
                every { application.type.toShortName() } returns "tserver"

                val accepted = Services.accept(application, listOf("NonMatchingType"))
                accepted shouldBe false
            }
        }

        "given a matching server application type filter should  be accepted" {

            val application = mockk<CfgApplication>()
            every { application.isServer } returns CfgFlag.CFGTrue
            every { application.type } returns CfgAppType.CFGTServer

            staticMockk("com.nuecho.genesys.cli.GenesysExtensionsKt").use {
                every { application.getDefaultEndpoint() } returns Endpoint(ENDPOINT_URI)
                every { application.type.toShortName() } returns "tserver"

                val accepted = Services.accept(application, listOf(TYPE))
                accepted shouldBe true
            }
        }

        "given an server application should return a service object" {

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

                service shouldBe ServiceDefinition("foo", TYPE, VERSION, ENDPOINT_URI, true)
            }
        }

        "given a list of server application should generate proper JSON output" {

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

                result shouldBe defaultJsonObjectMapper().readTree(javaClass.getResourceAsStream("services.json"))
            }
        }

        "given an unreachable endpoint should return status as DOWN" {
            val result = Services.status(Endpoint(ENDPOINT_URI), true, SOCKET_TIMEOUT)

            result shouldBe Status.DOWN
        }
    }
}
