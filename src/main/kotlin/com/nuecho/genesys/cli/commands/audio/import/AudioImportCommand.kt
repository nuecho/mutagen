package com.nuecho.genesys.cli.commands.audio.import

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.github.kittinunf.fuel.core.DataPart
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpUpload
import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.commands.audio.Audio
import com.nuecho.genesys.cli.commands.audio.AudioServices.APPLICATION_JSON
import com.nuecho.genesys.cli.commands.audio.AudioServices.AUDIO_RESOURCES_PATH
import com.nuecho.genesys.cli.commands.audio.AudioServices.CONTENT_TYPE
import com.nuecho.genesys.cli.commands.audio.AudioServices.DESCRIPTION
import com.nuecho.genesys.cli.commands.audio.AudioServices.HTTP
import com.nuecho.genesys.cli.commands.audio.AudioServices.MESSAGE_AR_ID
import com.nuecho.genesys.cli.commands.audio.AudioServices.NAME
import com.nuecho.genesys.cli.commands.audio.AudioServices.TENANT_ID
import com.nuecho.genesys.cli.commands.audio.AudioServices.checkStatusCode
import com.nuecho.genesys.cli.commands.audio.AudioServices.login
import com.nuecho.genesys.cli.commands.audio.AudioServices.toJson
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
    // URL Component
    const val UPLOAD_PATH = "/upload/?callback="

    // Status Codes
    const val CREATE_MESSAGE_SUCCESS_CODE = 302
    const val UPLOAD_AUDIO_SUCCESS_CODE = 200

    // Header
    const val LOCATION = "Location"

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
                if (audioPath.isEmpty() || personality == MESSAGE_AR_ID || personality == TENANT_ID) {
                    continue
                }

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

class AudioImportException(message: String) : Exception(message)

private data class CreateMessageRequest(
    val name: String,
    val description: String,
    val type: String = "ANNOUNCEMENT",
    val privateResource: Boolean = false
)
