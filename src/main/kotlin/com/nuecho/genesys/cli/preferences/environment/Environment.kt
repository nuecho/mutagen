package com.nuecho.genesys.cli.preferences.environment

import com.nuecho.genesys.cli.GenesysServices

data class Environment(
    val host: String,
    val port: Int = GenesysServices.DEFAULT_SERVER_PORT,
    val tls: Boolean = GenesysServices.DEFAULT_USE_TLS,
    val user: String,
    val password: String,
    val application: String = GenesysServices.DEFAULT_APPLICATION_NAME
)
