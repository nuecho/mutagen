package com.nuecho.genesys.cli

import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.Service
import com.nuecho.genesys.cli.services.withService
import picocli.CommandLine

abstract class ConfigServerCommand : GenesysCliCommand() {
    @CommandLine.Option(
        names = ["--trust-insecure-certificate"],
        description = ["Don't validate the server's TLS certificate."]
    )
    private var trustInsecureCertificate = false

    @Suppress("UNCHECKED_CAST")
    internal fun <T> withEnvironmentConfService(function: (service: ConfService) -> T): T =
        withService(
            ConfService(
                environment = getGenesysCli().loadEnvironment(),
                checkCertificate = !trustInsecureCertificate
            ),
            function as (service: Service) -> T
        )
}
