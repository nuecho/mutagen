package com.nuecho.genesys.cli.preferences.environment

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.nuecho.genesys.cli.preferences.Password
import com.nuecho.genesys.cli.services.GenesysServices

data class Environment(
    val host: String,
    val port: Int = GenesysServices.DEFAULT_SERVER_PORT,
    val tls: Boolean = GenesysServices.DEFAULT_USE_TLS,
    val user: String,
    @JsonProperty("password")
    var rawPassword: String?,
    val application: String = GenesysServices.DEFAULT_APPLICATION_NAME
) {
    var password: String?
        @JsonIgnore
        get() = when {
            Password.isEncrypted(rawPassword) -> Password.decrypt(rawPassword!!)
            else -> rawPassword
        }
        @JsonIgnore
        set(value) {
            rawPassword = value
        }
}
