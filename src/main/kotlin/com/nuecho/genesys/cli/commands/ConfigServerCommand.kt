package com.nuecho.genesys.cli.commands

import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.Services.withService
import picocli.CommandLine

abstract class ConfigServerCommand : GenesysCliCommand() {
    @CommandLine.Option(
        names = ["--trust-insecure-certificate"],
        description = ["Don't validate the server's TLS certificate."]
    )
    private var trustInsecureCertificate = false

    @Suppress("UNCHECKED_CAST")
    internal fun <T> withEnvironmentConfService(function: (service: ConfService, environment: Environment) -> T): T {
        val environment = getGenesysCli().loadEnvironment(password)
        val service = ConfService(
            environment = environment,
            checkCertificate = !trustInsecureCertificate
        )
        return withService(service) { function(service, environment) }
    }
}
