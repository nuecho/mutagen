package com.nuecho.genesys.cli.commands.audio.export

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.github.kittinunf.fuel.core.FuelManager
import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.Logging.warn
import com.nuecho.genesys.cli.commands.audio.ArmMessage
import com.nuecho.genesys.cli.commands.audio.Audio
import com.nuecho.genesys.cli.commands.audio.AudioRequestInfo
import com.nuecho.genesys.cli.commands.audio.AudioServices.HTTP
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
        description = ["Comma separated list of which personalities' audios to export (10,11,12,...)."]
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

    override fun execute(): Int {
        AudioExport.exportAudios(
            getGenesysCli().loadEnvironment(),
            outputFile!!,
            withAudios,
            personalitiesIds
        )

        return 0
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
        val csvSchema = buildCsvSchema(personalities)
        val messages = getMessagesData(gaxUrl)
        val audioDirectory = (outputFile.parentFile ?: File(System.getProperty("user.dir"))).apply { createDirectory() }

        writeCsv(messages, csvSchema, outputFile.outputStream())

        if (withAudios) {
            val missingPersonalitiesIds = getMissingPersonalitiesIds(personalities, selectedPersonalitiesIds)
            if (missingPersonalitiesIds.isNotEmpty()) {
                warn { "$missingPersonalitiesIds do not refer to existing personalities" }
            }

            val audioFilesInfos = getAudioMap(messages, selectedPersonalitiesIds, audioDirectory.path)
            downloadAudioFiles(gaxUrl, audioFilesInfos)
        }
    }

    internal fun writeCsv(armMessages: List<ArmMessage>, csvSchema: CsvSchema, outputStream: OutputStream) =
        CsvMapper()
            .writerFor(Message::class.java)
            .with(csvSchema)
            .writeValues(outputStream)
            .writeAll(armMessages.map { Message(it) }.sortedBy { it.messageArId })

    internal fun buildCsvSchema(personalities: Set<Personality>): CsvSchema {
        val schemaBuilder = CsvSchema.Builder()
            .addColumnsFrom(CsvMapper().schemaFor(Message::class.java))
            .setLineSeparator(System.getProperty("line.separator"))

        for (personality in personalities) {
            schemaBuilder.addColumn(personality.personalityId)
        }

        return schemaBuilder.build().withHeader()
    }

    internal fun getAudioMap(
        messages: List<ArmMessage>,
        selectedPersonalities: Set<String>?,
        audioDirectoryPath: String
    ): Map<String, AudioRequestInfo> {
        val audioFilesMap = mutableMapOf<String, AudioRequestInfo>()

        for (message in messages) {
            for (file in message.ownResourceFiles) {
                if (!isSelectedPersonality(file.personality.personalityId, selectedPersonalities))
                    continue

                audioFilesMap.set(
                    "$audioDirectoryPath/${file.personality.personalityId}/${message.name}.wav",
                    AudioRequestInfo(message.id, file.id)
                )
            }
        }

        return audioFilesMap
    }

    internal fun isSelectedPersonality(personalityId: String, selectedPersonalities: Set<String>?) =
        selectedPersonalities == null || selectedPersonalities.contains(personalityId)

    internal fun getMissingPersonalitiesIds(personalities: Set<Personality>, selectedPersonalitiesIds: Set<String>?) =
        selectedPersonalitiesIds?.subtract(personalities.map { it.personalityId }) ?: emptySet()

    private fun downloadAudioFiles(
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
        if (!this.exists() && !this.mkdirs())
            throw AudioServicesException("Failed to create directory $this")
    }
}
