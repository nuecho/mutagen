/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nuecho.mutagen.cli.commands.services

import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.nuecho.mutagen.cli.CliOutputCaptureWrapper
import com.nuecho.mutagen.cli.core.defaultJsonGenerator
import com.nuecho.mutagen.cli.core.defaultJsonObjectMapper
import com.nuecho.mutagen.cli.getDefaultEndpoint
import com.nuecho.mutagen.cli.toShortName
import io.mockk.every
import io.mockk.mockk
import io.mockk.staticMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.startsWith
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

        assertThat(output, startsWith(USAGE_PREFIX))
    }

    @Test
    fun `given an application name should return a short name`() {

        val application = mockk<CfgApplication>()
        every { application.type } returns CfgAppType.CFGTServer

        val shortName = application.type.toShortName()
        assertThat(shortName, equalTo(TYPE))
    }

    @Test
    fun `given a server application should be accepted`() {

        val application = mockk<CfgApplication>()
        every { application.isServer } returns CfgFlag.CFGTrue

        staticMockk("com.nuecho.mutagen.cli.GenesysExtensionsKt").use {
            every { application.getDefaultEndpoint() } returns Endpoint(ENDPOINT_URI)

            val accepted = Services.accept(application)
            assertThat(accepted, `is`(true))
        }
    }

    @Test
    fun `given a non-server application should not be accepted`() {

        val application = mockk<CfgApplication>()

        every { application.isServer } returns CfgFlag.CFGFalse

        val accepted = Services.accept(application)
        assertThat(accepted, `is`(false))
    }

    @Test
    fun `given a non-matching server application type filter should not be accepted`() {

        val application = mockk<CfgApplication>()
        every { application.isServer } returns CfgFlag.CFGTrue
        every { application.type } returns CfgAppType.CFGTServer

        staticMockk("com.nuecho.mutagen.cli.GenesysExtensionsKt").use {
            every { application.getDefaultEndpoint() } returns Endpoint(ENDPOINT_URI)
            every { application.type.toShortName() } returns "tserver"

            val accepted = Services.accept(application, listOf("NonMatchingType"))
            assertThat(accepted, `is`(false))
        }
    }

    @Test
    fun `given a matching server application type filter should  be accepted`() {

        val application = mockk<CfgApplication>()
        every { application.isServer } returns CfgFlag.CFGTrue
        every { application.type } returns CfgAppType.CFGTServer

        staticMockk("com.nuecho.mutagen.cli.GenesysExtensionsKt").use {
            every { application.getDefaultEndpoint() } returns Endpoint(ENDPOINT_URI)
            every { application.type.toShortName() } returns "tserver"

            val accepted = Services.accept(application, listOf(TYPE))
            assertThat(accepted, `is`(true))
        }
    }

    @Test
    fun `given a server application should return a service object`() {

        val application = mockk<CfgApplication>()
        every { application.isServer } returns CfgFlag.CFGTrue
        every { application.type } returns CfgAppType.CFGTServer
        every { application.version } returns VERSION
        every { application.name } returns "foo"
        every { application.isPrimary } returns CfgFlag.CFGTrue

        staticMockk("com.nuecho.mutagen.cli.GenesysExtensionsKt").use {
            every { application.getDefaultEndpoint()!!.uri } returns ENDPOINT_URI
            every { application.type.toShortName() } returns "tserver"

            val service = Services.toServiceDefinition(application)

            assertThat(service, equalTo(ServiceDefinition("foo", TYPE, VERSION, ENDPOINT_URI, true)))
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

        staticMockk("com.nuecho.mutagen.cli.GenesysExtensionsKt").use {
            every { application.getDefaultEndpoint()!!.uri } returns ENDPOINT_URI
            every { application.type.toShortName() } returns "tserver"

            val output = ByteArrayOutputStream()

            Services().writeServices(listOf(application), defaultJsonGenerator(output))

            val result = defaultJsonObjectMapper().readTree(String(output.toByteArray()))

            assertThat(result, equalTo(defaultJsonObjectMapper().readTree(javaClass.getResourceAsStream("services.json"))))
        }
    }

    @Test
    fun `given an unreachable endpoint should return status as DOWN`() {
        val result = Services.status(Endpoint(ENDPOINT_URI), true, SOCKET_TIMEOUT)
        assertThat(result, equalTo(Status.DOWN))
    }
}
