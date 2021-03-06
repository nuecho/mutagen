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

import com.fasterxml.jackson.core.JsonGenerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.queries.CfgApplicationQuery
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.nuecho.mutagen.cli.Logging.debug
import com.nuecho.mutagen.cli.Logging.info
import com.nuecho.mutagen.cli.MutagenCli
import com.nuecho.mutagen.cli.commands.ConfigServerCommand
import com.nuecho.mutagen.cli.core.defaultJsonGenerator
import com.nuecho.mutagen.cli.getDefaultEndpoint
import com.nuecho.mutagen.cli.preferences.environment.Environment
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.toShortName
import picocli.CommandLine
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI

const val DEFAULT_SOCKET_TIMEOUT = 200

@CommandLine.Command(
    name = "services",
    description = ["Discover services"],
    showDefaultValues = true
)
class Services : ConfigServerCommand() {

    @CommandLine.ParentCommand
    private var mutagenCli: MutagenCli? = null

    @CommandLine.Option(
        names = ["--types"],
        split = ",",
        description = ["Comma separated list of server application types to filter on " +
                "(TServer,StatServer,OrchestrationServer,...)."]
    )
    private var types: List<String>? = null
        get() = field?.map { it.toLowerCase() }

    @CommandLine.Option(
        names = ["--show-status"],
        description = ["Show service status."]
    )
    private var showStatus: Boolean = false

    @CommandLine.Option(
        names = ["--timeout"],
        description = ["Set socket connection timeout (ms)."]
    )
    private var socketTimeout: Int = DEFAULT_SOCKET_TIMEOUT

    override fun execute(): Int {
        info { "Discovering services" }
        withEnvironmentConfService { service: ConfService, _: Environment ->
            val applications = service.retrieveMultipleObjects(
                CfgApplication::class.java,
                CfgApplicationQuery()
            ) ?: emptyList()

            writeServices(applications)
        }

        return 0
    }

    internal fun writeServices(
        applications: Collection<CfgApplication>,
        jsonGenerator: JsonGenerator = defaultJsonGenerator()
    ) {

        info { "Filtering over ${applications.size} potential service candidate(s)." }

        jsonGenerator.use {
            jsonGenerator.writeStartArray()
            applications
                .filter { application -> accept(application, types) }
                .map { application -> toServiceDefinition(application, showStatus, socketTimeout) }
                .forEach { service ->
                    jsonGenerator.writeObject(service)
                }
            jsonGenerator.writeEndArray()
        }
    }

    override fun getMutagenCli() = mutagenCli!!

    companion object {

        internal fun accept(application: CfgApplication, types: List<String>? = null): Boolean {

            if (application.isServer != CfgFlag.CFGTrue) return false

            if (application.getDefaultEndpoint() == null) return false

            if (types == null) return true // no filter applied

            return types.contains(application.type.toShortName())
        }

        internal fun toServiceDefinition(
            application: CfgApplication,
            showStatus: Boolean = false, socketTimeout: Int = DEFAULT_SOCKET_TIMEOUT
        ) =
            ServiceDefinition(
                name = application.name,
                type = application.type.toShortName(),
                version = application.version.toString(),
                endpoint = application.getDefaultEndpoint()?.uri,
                isPrimary = application.isPrimary == CfgFlag.CFGTrue,
                status = status(application.getDefaultEndpoint(), showStatus, socketTimeout)
            )

        internal fun status(
            endpoint: Endpoint?,
            showStatus: Boolean = false, socketTimeout: Int = DEFAULT_SOCKET_TIMEOUT
        ): Status? {
            if (endpoint == null) return null
            if (!showStatus) return null

            info { "Trying TCP connection to ${endpoint.host}:${endpoint.port}." }
            return try {
                Socket().connect(InetSocketAddress(endpoint.host, endpoint.port), socketTimeout)
                Status.UP
            } catch (e: Throwable) {
                debug { e.message }
                Status.DOWN
            }
        }
    }
}

data class ServiceDefinition(
    val name: String, val type: String, val version: String,
    val endpoint: URI?, val isPrimary: Boolean = true, val status: Status? = null
)

enum class Status { DOWN, UP }
