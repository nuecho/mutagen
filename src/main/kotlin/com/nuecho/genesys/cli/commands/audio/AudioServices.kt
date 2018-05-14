package com.nuecho.genesys.cli.commands.audio

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.nuecho.genesys.cli.Logging

object AudioServices {

    // URL Components
    const val HTTP = "http://"
    const val GAX_BASE_PATH = "/gax/api"
    const val AUDIO_RESOURCES_PATH = "$GAX_BASE_PATH/arm/audioresources"
    const val LOGIN_PATH = "$GAX_BASE_PATH/session/login"

    const val LOGIN_SUCCESS_CODE = 204

    // Message Map Keys
    const val NAME = "name"
    const val DESCRIPTION = "description"
    const val MESSAGE_AR_ID = "messageArId"
    const val TENANT_ID = "tenantId"

    // Headers
    const val CONTENT_TYPE = "Content-Type"
    const val APPLICATION_JSON = "application/json"

    fun login(user: String, password: String, encryptPassword: Boolean, gaxUrl: String) {
        val effectivePassword = if (encryptPassword) encryptPassword(password) else password

        "$gaxUrl$LOGIN_PATH"
            .httpPost()
            .header(CONTENT_TYPE to APPLICATION_JSON)
            .body(LoginRequest(user, effectivePassword, encryptPassword).toJson())
            .responseString()
            .let { (request, response, result) ->
                checkStatusCode(LOGIN_SUCCESS_CODE, "Failed to log in to GAX.", request, response, result)
            }
    }

    // Based on GAX quickEncrypt JS function
    @Suppress("MagicNumber")
    private fun encryptPassword(password: String): String {
        val chars = password.chars().toArray()
        val encryptedChars = IntArray(chars.size * 2)

        for (charIndex in 0 until password.length) {
            val random = (Math.round(Math.random() * 122) + 68).toInt()
            val encryptedCharIndex = charIndex * 2
            encryptedChars[encryptedCharIndex] = chars[charIndex] + random
            encryptedChars[encryptedCharIndex + 1] = random
        }

        return String(encryptedChars.map { it.toChar() }.toCharArray())
    }

    fun checkStatusCode(
        statusCode: Int,
        errorMessage: String,
        request: Request,
        response: Response,
        result: Result<*, *>
    ) {
        if (response.statusCode != statusCode) {
            Logging.debug {
                """
                |Request: $request
                |Response: $response
                |Result: $result
                """.trimMargin()
            }
            throw AudioServicesException(errorMessage)
        }
    }

    fun Any.toJson(): String = jacksonObjectMapper().writeValueAsString(this)
}

class AudioServicesException(message: String) : Exception(message)

private data class LoginRequest(
    val username: String,
    val password: String,
    @get:JsonProperty("isPasswordEncrypted")
    val isPasswordEncrypted: Boolean
)
