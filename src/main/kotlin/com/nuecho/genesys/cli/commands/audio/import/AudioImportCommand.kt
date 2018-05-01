package com.nuecho.genesys.cli.commands.audio.import

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.kittinunf.fuel.core.DataPart
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpUpload
import com.github.kittinunf.result.Result
import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging.debug
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.commands.audio.Audio
import com.nuecho.genesys.cli.preferences.environment.Environment
import picocli.CommandLine
import java.io.File
import java.io.InputStream
import java.lang.System.currentTimeMillis
import java.net.CookieHandler
import java.net.CookieManager

@CommandLine.Command(
    name = "import",
    description = ["Import audio files in ARM"]
)
class AudioImportCommand : GenesysCliCommand() {
    @CommandLine.ParentCommand
    private var audio: Audio? = null

    @CommandLine.Parameters(
        arity = "1",
        index = "0",
        paramLabel = "inputFile",
        description = ["Input audio description file."]
    )
    private var inputFile: File? = null

    override fun getGenesysCli(): GenesysCli = audio!!.getGenesysCli()

    override fun execute() {
        AudioImport.importAudios(
            getGenesysCli().loadEnvironment(),
            inputFile!!.inputStream(),
            inputFile!!.absoluteFile.parentFile
        )
    }
}

object AudioImport {
    // URL Components
    private const val HTTP = "http://"
    private const val GAX_BASE_PATH = "/gax/api"
    const val LOGIN_PATH = "$GAX_BASE_PATH/session/login"
    const val AUDIO_RESOURCES_PATH = "$GAX_BASE_PATH/arm/audioresources/"
    const val UPLOAD_PATH = "/upload/?callback="

    // Status Codes
    const val LOGIN_SUCCESS_CODE = 204
    const val CREATE_MESSAGE_SUCCESS_CODE = 302
    const val UPLOAD_AUDIO_SUCCESS_CODE = 200

    // Message Map Keys
    private const val NAME = "name"
    private const val DESCRIPTION = "description"

    // Headers
    const val LOCATION = "Location"
    const val CONTENT_TYPE = "Content-Type"
    const val APPLICATION_JSON = "application/json"

    fun importAudios(environment: Environment, audioData: InputStream, audioDirectory: File) {
        // To disable the automatic redirection
        FuelManager.instance.removeAllResponseInterceptors()
        CookieHandler.setDefault(CookieManager())

        val gaxUrl = "$HTTP${environment.host}:${environment.port}"
        var callbackSequenceNumber = 1
        val messages = readAudioData(audioData)

        info { "Logging in to GAX as '${environment.user}'." }
        login(environment.user, environment.password!!, true, gaxUrl)

        messages.forEach {
            val message = it.toMutableMap()
            val name = message.remove(NAME) ?: throw AudioImportException("Missing $NAME column in audio data file")
            val description = message.remove(DESCRIPTION) ?: ""

            info { "Creating message '$name'." }
            val messageUrl = createMessage(name, description, gaxUrl)

            for ((personality, audioPath) in message) {
                info { "Uploading '$audioPath'." }
                uploadAudio(
                    messageUrl = messageUrl,
                    audioFile = File(audioDirectory, audioPath),
                    personality = personality,
                    callbackSequenceNumber = callbackSequenceNumber++
                )
            }
        }
    }

    internal fun readAudioData(inputStream: InputStream) =
        CsvMapper()
            .readerFor(Map::class.java)
            .with(CsvSchema.emptySchema().withHeader())
            .readValues<Map<String, String>>(inputStream)

    internal fun login(user: String, password: String, encryptPassword: Boolean, gaxUrl: String) {
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
                        ?: throw AudioImportException("Failed to recover message '$name' location.")
            }
    }

    internal fun uploadAudio(
        messageUrl: String,
        audioFile: File,
        personality: String,
        callbackSequenceNumber: Int
    ) {
        val callback = "parent._callbacks._${currentTimeMillis()}_$callbackSequenceNumber"
        val dataPart = DataPart(audioFile, "requestData", "audio/wav")

        "$messageUrl$UPLOAD_PATH$callback"
            .httpUpload(parameters = listOf("personalityId" to personality))
            .dataParts { _, _ -> listOf(dataPart) }
            .responseString()
            .let { (request, response, result) ->
                checkStatusCode(
                    UPLOAD_AUDIO_SUCCESS_CODE,
                    "Failed to upload audio '${audioFile.name}'.",
                    request,
                    response,
                    result
                )
            }
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

private fun checkStatusCode(
    statusCode: Int,
    errorMessage: String,
    request: Request,
    response: Response,
    result: Result<*, *>
) {
    if (response.statusCode != statusCode) {
        debug {
            """
            |Request: $request
            |Response: $response
            |Result: $result
            """.trimMargin()
        }
        throw AudioImportException(errorMessage)
    }
}

class AudioImportException(message: String) : Exception(message)

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

private fun Any.toJson(): String = jacksonObjectMapper().writeValueAsString(this)
