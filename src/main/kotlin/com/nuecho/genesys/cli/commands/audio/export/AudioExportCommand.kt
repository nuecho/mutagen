package com.nuecho.genesys.cli.commands.audio.export

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpDownload
import com.github.kittinunf.fuel.httpGet
import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.Logging.warn
import com.nuecho.genesys.cli.commands.audio.Audio
import com.nuecho.genesys.cli.commands.audio.AudioServices.APPLICATION_JSON
import com.nuecho.genesys.cli.commands.audio.AudioServices.AUDIO_RESOURCES_PATH
import com.nuecho.genesys.cli.commands.audio.AudioServices.CONTENT_TYPE
import com.nuecho.genesys.cli.commands.audio.AudioServices.DESCRIPTION
import com.nuecho.genesys.cli.commands.audio.AudioServices.GAX_BASE_PATH
import com.nuecho.genesys.cli.commands.audio.AudioServices.HTTP
import com.nuecho.genesys.cli.commands.audio.AudioServices.MESSAGE_AR_ID
import com.nuecho.genesys.cli.commands.audio.AudioServices.NAME
import com.nuecho.genesys.cli.commands.audio.AudioServices.TENANT_ID
import com.nuecho.genesys.cli.commands.audio.AudioServices.checkStatusCode
import com.nuecho.genesys.cli.commands.audio.AudioServices.login
import com.nuecho.genesys.cli.commands.audio.AudioServicesException
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import picocli.CommandLine
import java.io.File
import java.io.OutputStream
import java.net.CookieHandler
import java.net.CookieManager
import java.util.ArrayList

@CommandLine.Command(
    name = "export",
    description = ["Export audio files from ARM"]
)
class AudioExportCommand : GenesysCliCommand() {
    @CommandLine.ParentCommand
    private var audio: Audio? = null

    @CommandLine.Option(
        names = ["--with-audios"],
        description = ["Download messages' audio files."]
    )
    private var withAudios: Boolean = false

    @CommandLine.Option(
        names = ["--personalities-ids"],
        split = ",",
        description = ["Comma separated list of which personalities' audios to export " +
                "(10001,10002,10015,...)."]
    )
    private var personalitiesIds: Set<String>? = null

    @CommandLine.Parameters(
        arity = "1",
        index = "0",
        paramLabel = "outputFile",
        description = ["Output audio description file."]
    )
    private var outputFile: File? = null

    override fun getGenesysCli(): GenesysCli = audio!!.getGenesysCli()

    override fun execute() {

        AudioExport.exportAudios(
            getGenesysCli().loadEnvironment(),
            outputFile!!.outputStream(),
            outputFile!!.parentFile.path,
            withAudios,
            personalitiesIds
        )
    }
}

object AudioExport {
    // URL Components
    const val PERSONALITIES_PATH = "$GAX_BASE_PATH/arm/personalities/"
    const val AUDIO_MESSAGES_PATH = "$AUDIO_RESOURCES_PATH/?tenantList=1"
    const val FILES_PATH = "/files"
    const val AUDIO_PATH = "/audio?"

    // Status Code
    const val DOWNLOAD_AUDIO_SUCCESS_CODE = 200

    fun exportAudios(
        environment: Environment,
        audioData: OutputStream,
        audioDirectoryPath: String,
        withAudios: Boolean,
        selectedPersonalitiesIds: Set<String>?
    ) {
        FuelManager.instance.removeAllResponseInterceptors()
        CookieHandler.setDefault(CookieManager())

        val gaxUrl = "$HTTP${environment.host}:${environment.port}"

        info { "Logging in to GAX as '${environment.user}'." }
        login(environment.user, environment.password!!, true, gaxUrl)

        val personalities = getPersonalities(gaxUrl)
        val csvSchemaBuilder = getSchemaBuilder(personalities)
        val responseItems = getMessagesData(gaxUrl)
        writeAudioData(responseItems, csvSchemaBuilder, audioData)

        if (withAudios) {
            val missingPersonalitiesIds = getMissingPersonalitiesIds(personalities, selectedPersonalitiesIds)
            if (!missingPersonalitiesIds.isEmpty()) {
                warn { "$missingPersonalitiesIds do not refer to existing personalities" }
            }

            val audioFilesInfos = getAudioMap(responseItems, selectedPersonalitiesIds, audioDirectoryPath)
            downloadAudioFiles(gaxUrl, audioFilesInfos)
        }
    }

    internal fun getMessagesData(gaxUrl: String): List<Message> {
        val responseItemListType = TypeFactory
            .defaultInstance()
            .constructCollectionType(List::class.java, Message::class.java)

        "$gaxUrl$AUDIO_MESSAGES_PATH"
            .httpGet()
            .header(CONTENT_TYPE to APPLICATION_JSON)
            .responseString()
            .let { (request, response, result) ->
                checkStatusCode(
                    DOWNLOAD_AUDIO_SUCCESS_CODE,
                    "Failed to download audio messages.",
                    request,
                    response,
                    result
                )

                return defaultJsonObjectMapper().readValue(
                    response.data.inputStream(),
                    responseItemListType
                )
            }
    }

    internal fun getPersonalities(gaxUrl: String): Set<Personality> {
        val personalityListType = TypeFactory
            .defaultInstance()
            .constructCollectionType(Set::class.java, Personality::class.java)

        "$gaxUrl$PERSONALITIES_PATH"
            .httpGet()
            .header(CONTENT_TYPE to APPLICATION_JSON)
            .responseString()
            .let { (request, response, result) ->
                checkStatusCode(
                    DOWNLOAD_AUDIO_SUCCESS_CODE,
                    "Failed to download personalities.",
                    request,
                    response,
                    result
                )

                return defaultJsonObjectMapper().readValue(
                    response.data.inputStream(),
                    personalityListType
                )
            }
    }

    internal fun writeAudioData(
        responseItems: List<Message>,
        csvSchemaBuilder: CsvSchema.Builder,
        outputStream: OutputStream
    ) {
        val mapper = CsvMapper()
        mapper.enable(JsonGenerator.Feature.IGNORE_UNKNOWN)

        val messages = responseItems.map { item ->
            val message: MutableMap<String, String> = mutableMapOf(
                NAME to item.name,
                DESCRIPTION to item.description,
                MESSAGE_AR_ID to item.arId.toString(),
                TENANT_ID to item.tenantId.toString()
            )

            for (audioResource in item.ownResourceFiles) {
                message[audioResource.personality.id] = audioResource.originalFilename
            }
            message
        }

        CsvMapper()
            .writerFor(Map::class.java)
            .with(
                csvSchemaBuilder
                    .setLineSeparator(System.getProperty("line.separator"))
                    .build()
                    .sortedBy(NAME, DESCRIPTION, MESSAGE_AR_ID, TENANT_ID)
                    .withHeader()
            )
            .writeValues(outputStream)
            .writeAll(messages)
    }

    internal fun getSchemaBuilder(personalities: Set<Personality>): CsvSchema.Builder {

        val csvSchemaBuilder = CsvSchema.Builder()

        csvSchemaBuilder.addColumn(NAME)
        csvSchemaBuilder.addColumn(DESCRIPTION)
        csvSchemaBuilder.addColumn(TENANT_ID)
        csvSchemaBuilder.addColumn(MESSAGE_AR_ID)

        for (personality in personalities) {
            csvSchemaBuilder.addColumn(personality.id.toString())
        }

        return csvSchemaBuilder
    }

    internal fun downloadAudioFiles(
        gaxUrl: String,
        audioFilesIds: Map<String, AudioRequestInfo>
    ) {
        audioFilesIds.forEach { filePath, audioRequestInfo ->
            val file = File(filePath)

            file.parentFile.createDirectory()

            file.createNewFile()
            downloadAudioFile(gaxUrl, audioRequestInfo, file)
        }
    }

    internal fun downloadAudioFile(
        gaxUrl: String,
        audioFileInfo: AudioRequestInfo,
        audioFile: File
    ) {
        "$gaxUrl$AUDIO_RESOURCES_PATH/${audioFileInfo.id}$FILES_PATH/${audioFileInfo.fileId}$AUDIO_PATH"
            .httpDownload()
            .destination { response, _ -> audioFile }
            .responseString { _, response, _ ->
                if (response.statusCode != DOWNLOAD_AUDIO_SUCCESS_CODE) {
                    warn { "${audioFile.path} download failed, status code: ${response.statusCode}" }
                    audioFile.delete()
                }
            }
    }

    private fun File.createDirectory() {

        if (!this.exists() &&
            !this.mkdirs()
        ) {
            throw AudioServicesException("Failed to create directory $this")
        }
    }

    internal fun getAudioMap(
        responseItems: List<Message>,
        selectedPersonalities: Set<String>?,
        audioDirectoryPath: String
    ): Map<String, AudioRequestInfo> {
        val audioFilesMap = mutableMapOf<String, AudioRequestInfo>()
        val filePaths = mutableMapOf<String, Int>()

        responseItems.forEach { (_, _, id, _, _, ownResourceFiles) ->
            for (file in ownResourceFiles) {
                if (!isSelectedPersonality(file.personality.id, selectedPersonalities)) {
                    continue
                }

                val originalFileName = file.originalFilename.substringBefore(".wav")
                val originalFilePath = "$audioDirectoryPath/${file.personality.id}/$originalFileName"
                var filePath = originalFilePath
                var nbFilesWithPath = filePaths.getOrDefault(originalFilePath, 0)

                if (nbFilesWithPath != 0) {
                    filePath = "$originalFilePath($nbFilesWithPath)"
                    val fileName = "${filePath.substringAfterLast("/")}.wav"
                    warn { "$originalFileName already exists and has been renamed to $fileName" }
                }

                filePaths.set(originalFilePath, ++nbFilesWithPath)

                audioFilesMap.set("$filePath.wav", AudioRequestInfo(id, file.id))
            }
        }

        return audioFilesMap
    }

    internal fun isSelectedPersonality(personalityId: String, selectedPersonalities: Set<String>?) =
        selectedPersonalities == null || selectedPersonalities.contains(personalityId)

    internal fun getMissingPersonalitiesIds(
        personalities: Set<Personality>,
        selectedPersonalitiesIds: Set<String>?
    ) = if (selectedPersonalitiesIds == null) emptySet()
        else selectedPersonalitiesIds.subtract(personalities.map { it.id })
}

internal data class AudioRequestInfo(
    val id: String,
    val fileId: String
)

internal data class Message(
    val name: String,
    val description: String,
    val id: String,
    val arId: Int,
    val tenantId: Int,
    val ownResourceFiles: ArrayList<OwnResourceFile>
)

internal data class OwnResourceFile(
    val id: String,
    val originalFilename: String,
    val personality: Personality
)

internal data class Personality(
    val id: String
)
