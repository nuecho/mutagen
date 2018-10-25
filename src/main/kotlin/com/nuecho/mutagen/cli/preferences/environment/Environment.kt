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

package com.nuecho.mutagen.cli.preferences.environment

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.nuecho.mutagen.cli.preferences.SecurePassword
import com.nuecho.mutagen.cli.services.GenesysServices

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
        get() = if (rawPassword == null) null
        else SecurePassword(rawPassword!!.toCharArray())
        @JsonIgnore
        set(password) {
            rawPassword = password?.value
        }
}
