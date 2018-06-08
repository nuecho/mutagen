package com.nuecho.genesys.cli.commands.audio

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.kittinunf.fuel.core.DataPart
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpDownload
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpUpload
import com.github.kittinunf.result.Result
import com.nuecho.genesys.cli.Logging
import com.nuecho.genesys.cli.Logging.warn
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import java.io.File
import java.util.ArrayList

object AudioServices {

    // URL Components
    const val HTTP = "http://"
    const val GAX_BASE_PATH = "/gax/api"
    const val LOGIN_PATH = "$GAX_BASE_PATH/session/login"
    const val UPLOAD_PATH = "/upload/?callback="
    const val AUDIO_RESOURCES_PATH = "$GAX_BASE_PATH/arm/audioresources"
    const val PERSONALITIES_PATH = "$GAX_BASE_PATH/arm/personalities/"
    const val AUDIO_MESSAGES_PATH = "$AUDIO_RESOURCES_PATH/?tenantList=1"
    const val FILES_PATH = "/files"
    const val AUDIO_PATH = "/audio?"

    // Success codes
    const val LOGIN_SUCCESS_CODE = 204
    const val CREATE_MESSAGE_SUCCESS_CODE = 302
    const val SUCCESS_CODE = 200

    // Message Map Keys
    const val NAME = "name"
    const val DESCRIPTION = "description"
    const val MESSAGE_AR_ID = "messageArId"
    const val TENANT_ID = "tenantId"

    // Headers
    const val CONTENT_TYPE = "Content-Type"
    const val APPLICATION_JSON = "application/json"
    const val LOCATION = "Location"

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

    internal fun createMessage(name: String, description: String, gaxUrl: String): String {
        "$gaxUrl$AUDIO_RESOURCES_PATH"
            .httpPost()
            .header(CONTENT_TYPE to APPLICATION_JSON)
            .body(CreateMessageRequest(name, description).toJson())
            .responseString()
            .let { (request, response, result) ->
                checkStatusCode(
                    CREATE_MESSAGE_SUCCESS_CODE,
                    "Failed to create message '$name'.",
                    request,
                    response,
                    result
                )

                return response.headers[LOCATION]?.first()
                        ?: throw AudioServicesException("Failed to recover message '$name' location.")
            }
    }

    internal fun uploadAudio(
        messageUrl: String,
        audioFile: File,
        personality: String,
        callbackSequenceNumber: Int
    ) {
        val callback = "parent._callbacks._${System.currentTimeMillis()}_$callbackSequenceNumber"
        val dataPart = DataPart(audioFile, "requestData", "audio/wav")

        "$messageUrl$UPLOAD_PATH$callback"
            .httpUpload(parameters = listOf("personalityId" to personality))
            .dataParts { _, _ -> listOf(dataPart) }
            .responseString()
            .let { (request, response, result) ->
                checkStatusCode(
                    SUCCESS_CODE,
                    "Failed to upload audio '${audioFile.name}'.",
                    request,
                    response,
                    result
                )
            }
    }

    fun getPersonalities(gaxUrl: String): Set<Personality> {
        val personalityListType = TypeFactory
            .defaultInstance()
            .constructCollectionType(Set::class.java, Personality::class.java)

        "$gaxUrl$PERSONALITIES_PATH"
            .httpGet()
            .header(CONTENT_TYPE to APPLICATION_JSON)
            .responseString()
            .let { (request, response, result) ->
                checkStatusCode(
                    SUCCESS_CODE,
                    "Failed to fetch personalities.",
                    request,
                    response,
                    result
                )

                return response.data.inputStream().use {
                    defaultJsonObjectMapper().readValue(it, personalityListType)
                }
            }
    }

    fun getMessagesData(gaxUrl: String): List<Message> {
        val messageListType = TypeFactory
            .defaultInstance()
            .constructCollectionType(List::class.java, Message::class.java)

        "$gaxUrl$AUDIO_MESSAGES_PATH"
            .httpGet()
            .header(CONTENT_TYPE to APPLICATION_JSON)
            .responseString()
            .let { (request, response, result) ->
                checkStatusCode(
                    SUCCESS_CODE,
                    "Failed to fetch audio messages.",
                    request,
                    response,
                    result
                )

                return response.data.inputStream().use {
                    defaultJsonObjectMapper().readValue(it, messageListType)
                }
            }
    }

    internal fun downloadAudioFile(
        gaxUrl: String,
        audioFileInfo: AudioRequestInfo,
        destination: File
    ) {

        "$gaxUrl$AUDIO_RESOURCES_PATH/${audioFileInfo.id}$FILES_PATH/${audioFileInfo.fileId}$AUDIO_PATH"
            .httpDownload()
            .destination { _, _ -> destination }
            .responseString ()
            .let { (_, response, _) ->
                if (response.statusCode != SUCCESS_CODE) {
                    warn {
                        "${destination.parentFile}/$destination download failed, status code: ${response.statusCode}"
                    }
                    destination.delete()
                }
            }
    }

    private fun checkStatusCode(
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

    private fun Any.toJson(): String = jacksonObjectMapper().writeValueAsString(this)
}

class AudioServicesException(message: String) : Exception(message)

private data class LoginRequest(
    val username: String,
    val password: String,
    @get:JsonProperty("isPasswordEncrypted")
    val isPasswordEncrypted: Boolean
)

private data class CreateMessageRequest(
    val name: String,
    val description: String,
    val type: String = "ANNOUNCEMENT",
    val privateResource: Boolean = false
)

data class Message(
    val name: String,
    val description: String,
    val id: String,
    val arId: Int,
    val tenantId: Int,
    val ownResourceFiles: ArrayList<OwnResourceFile>
)

data class OwnResourceFile(
    val id: String,
    val originalFilename: String,
    val personality: Personality
)

data class Personality(
    val personalityId: String,
    val id: String
)

internal data class AudioRequestInfo(
    val id: String,
    val fileId: String
)
