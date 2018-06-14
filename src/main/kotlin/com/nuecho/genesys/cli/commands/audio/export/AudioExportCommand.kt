package com.nuecho.genesys.cli.commands.audio.export

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.github.kittinunf.fuel.core.FuelManager
import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.Logging.warn
import com.nuecho.genesys.cli.commands.audio.Audio
import com.nuecho.genesys.cli.commands.audio.AudioRequestInfo
import com.nuecho.genesys.cli.commands.audio.AudioServices.DESCRIPTION
import com.nuecho.genesys.cli.commands.audio.AudioServices.HTTP
import com.nuecho.genesys.cli.commands.audio.AudioServices.MESSAGE_AR_ID
import com.nuecho.genesys.cli.commands.audio.AudioServices.NAME
import com.nuecho.genesys.cli.commands.audio.AudioServices.TENANT_ID
import com.nuecho.genesys.cli.commands.audio.AudioServices.downloadAudioFile
import com.nuecho.genesys.cli.commands.audio.AudioServices.getMessagesData
import com.nuecho.genesys.cli.commands.audio.AudioServices.getPersonalities
import com.nuecho.genesys.cli.commands.audio.AudioServices.login
import com.nuecho.genesys.cli.commands.audio.AudioServicesException
import com.nuecho.genesys.cli.commands.audio.Message
import com.nuecho.genesys.cli.commands.audio.Personality
import com.nuecho.genesys.cli.preferences.environment.Environment
import picocli.CommandLine
import java.io.File
import java.io.OutputStream
import java.net.CookieHandler
import java.net.CookieManager

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
            outputFile!!,
            withAudios,
            personalitiesIds
        )
    }
}

object AudioExport {

    fun exportAudios(
        environment: Environment,
        outputFile: File,
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
        val messages = getMessagesData(gaxUrl)
        val audioDirectory = (outputFile.parentFile ?: File(System.getProperty("user.dir"))).apply { createDirectory() }

        writeAudioData(messages, csvSchemaBuilder, outputFile.outputStream())

        if (withAudios) {
            val missingPersonalitiesIds = getMissingPersonalitiesIds(personalities, selectedPersonalitiesIds)
            if (missingPersonalitiesIds.isNotEmpty()) {
                warn { "$missingPersonalitiesIds do not refer to existing personalities" }
            }

            val audioFilesInfos = getAudioMap(messages, selectedPersonalitiesIds, audioDirectory.path)
            downloadAudioFiles(gaxUrl, audioFilesInfos)
        }
    }

    internal fun writeAudioData(
        messages: List<Message>,
        csvSchemaBuilder: CsvSchema.Builder,
        outputStream: OutputStream
    ) {
        val mapper = CsvMapper()
        mapper.enable(JsonGenerator.Feature.IGNORE_UNKNOWN)

        val csvRows = messages.map { item ->
            val csvRow: MutableMap<String, String> = mutableMapOf(
                NAME to item.name,
                DESCRIPTION to item.description,
                MESSAGE_AR_ID to item.arId.toString(),
                TENANT_ID to item.tenantId.toString()
            )

            for (audioResource in item.ownResourceFiles) {
                val personalityId = audioResource.personality.personalityId
                csvRow[personalityId] = "$personalityId/${item.name}.wav"
            }
            csvRow
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
            .writeAll(csvRows)
    }

    internal fun getSchemaBuilder(personalities: Set<Personality>): CsvSchema.Builder {

        val csvSchemaBuilder = CsvSchema.Builder()

        csvSchemaBuilder.addColumn(NAME)
        csvSchemaBuilder.addColumn(DESCRIPTION)
        csvSchemaBuilder.addColumn(TENANT_ID)
        csvSchemaBuilder.addColumn(MESSAGE_AR_ID)

        for (personality in personalities) {
            csvSchemaBuilder.addColumn(personality.personalityId)
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

    private fun File.createDirectory() {

        if (!this.exists() &&
            !this.mkdirs()
        ) {
            throw AudioServicesException("Failed to create directory $this")
        }
    }

    internal fun getAudioMap(
        messages: List<Message>,
        selectedPersonalities: Set<String>?,
        audioDirectoryPath: String
    ): Map<String, AudioRequestInfo> {
        val audioFilesMap = mutableMapOf<String, AudioRequestInfo>()

        messages.forEach { (name, _, id, _, _, ownResourceFiles) ->
            for (file in ownResourceFiles) {
                if (!isSelectedPersonality(file.personality.personalityId, selectedPersonalities)) {
                    continue
                }
                audioFilesMap.set(
                    "$audioDirectoryPath/${file.personality.personalityId}/$name.wav",
                    AudioRequestInfo(id, file.id)
                )
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
    else selectedPersonalitiesIds.subtract(personalities.map { it.personalityId })
}
