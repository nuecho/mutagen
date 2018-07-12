package com.nuecho.genesys.cli.preferences

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.Arrays.fill

@Suppress("UseDataClass")
class SecurePassword(private val characters: CharArray) {
    val value: String
        @JsonIgnore
        get() {
            val password = String(characters)
            fill(characters, ' ')
            return password.trim()
        }
}
