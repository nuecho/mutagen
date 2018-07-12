package com.nuecho.genesys.cli.preferences.environment

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.nuecho.genesys.cli.preferences.Passwords.decrypt
import com.nuecho.genesys.cli.preferences.Passwords.isEncrypted
import com.nuecho.genesys.cli.preferences.SecurePassword
import com.nuecho.genesys.cli.services.GenesysServices

data class Environment(
    val host: String,
    val port: Int = GenesysServices.DEFAULT_SERVER_PORT,
    val tls: Boolean = GenesysServices.DEFAULT_USE_TLS,
    val user: String,
    @JsonProperty("password")
    var rawPassword: String?,
    val application: String = GenesysServices.DEFAULT_APPLICATION_NAME,
    val encoding: String = "utf-8"
) {
    var password: SecurePassword?
        @JsonIgnore
        get() = when {
            rawPassword == null -> null
            isEncrypted(rawPassword) -> SecurePassword(decrypt(rawPassword!!).toCharArray())
            else -> SecurePassword(rawPassword!!.toCharArray())
        }
        @JsonIgnore
        set(password) {
            rawPassword = password?.value
        }
}
