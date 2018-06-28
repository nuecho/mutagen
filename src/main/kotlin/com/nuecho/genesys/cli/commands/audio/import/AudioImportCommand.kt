package com.nuecho.genesys.cli.commands.audio.import

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.core.FuelManager
import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.commands.audio.Audio
import com.nuecho.genesys.cli.commands.audio.AudioServices.HTTP
import com.nuecho.genesys.cli.commands.audio.AudioServices.createMessage
import com.nuecho.genesys.cli.commands.audio.AudioServices.getMessagesData
import com.nuecho.genesys.cli.commands.audio.AudioServices.getPersonalities
import com.nuecho.genesys.cli.commands.audio.AudioServices.login
import com.nuecho.genesys.cli.commands.audio.AudioServices.uploadAudio
import com.nuecho.genesys.cli.commands.audio.ArmMessage
import com.nuecho.genesys.cli.commands.audio.Personality
import com.nuecho.genesys.cli.commands.audio.Message
import com.nuecho.genesys.cli.preferences.environment.Environment
import picocli.CommandLine
import java.io.File
import java.io.InputStream
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

    override fun execute(): Int {
        AudioImport.importAudios(
            getGenesysCli().loadEnvironment(),
            inputFile!!.inputStream(),
            inputFile!!.absoluteFile.parentFile
        )

        return 0
    }
}

object AudioImport {

    fun importAudios(environment: Environment, audioData: InputStream, audioDirectory: File) {
        // To disable the automatic redirection
        FuelManager.instance.removeAllResponseInterceptors()
        CookieHandler.setDefault(CookieManager())

        val gaxUrl = "$HTTP${environment.host}:${environment.port}"
        var callbackSequenceNumber = 1
        val messages = readAudioData(audioData)

        info { "Logging in to GAX as '${environment.user}'." }
        login(environment.user, environment.password!!, true, gaxUrl)

        val existingMessages = getMessagesData(gaxUrl)
        checkDuplicatedMessagesNames(messages, existingMessages)

        val personalityIdToQueryIdMap = getPersonalitiesIdsMap(getPersonalities(gaxUrl))
        checkMissingPersonalities(messages, personalityIdToQueryIdMap)

        checkMissingAudioFiles(messages, audioDirectory)

        messages.sortedBy { it.messageArId }.forEach { message ->
            info { "Creating message '${message.name}'." }
            val messageUrl = createMessage(message.name, message.type, message.description, gaxUrl)
            val url = "${messageUrl.substringBeforeLast("/")}/audioresources/${messageUrl.substringAfterLast("/")}"

            message.audioByPersonality.forEach { (personality, audioPath) ->
                info { "Uploading '$audioPath'." }
                uploadAudio(
                    messageUrl = url,
                    audioFile = File(audioDirectory, audioPath),
                    personality = personalityIdToQueryIdMap[personality]!!,
                    callbackSequenceNumber = callbackSequenceNumber++
                )
            }
        }
    }

    internal fun readAudioData(inputStream: InputStream) =
        CsvMapper()
            .registerKotlinModule()
            .readerFor(Message::class.java)
            .with(CsvSchema.emptySchema().withHeader())
            .readValues<Message>(inputStream)
            .readAll()

    internal fun checkDuplicatedMessagesNames(
        messages: List<Message>,
        existingArmMessages: List<ArmMessage>
    ) {
        val armMessagesName = existingArmMessages.map { it.name.toLowerCase() }
        val invalidMessagesNames = messages
            .map { it.name.toLowerCase() }
            .filter { armMessagesName.contains(it) }
            .toList()

        checkForInvalidElements(invalidMessagesNames, "messages' names already exist on the gax server")
    }

    internal fun checkMissingAudioFiles(messages: List<Message>, audioDirectory: File) {
        val invalidAudioFiles = messages.flatMap { it.audioByPersonality.values }
            .map { filename -> File(audioDirectory, filename) }
            .filter { !it.exists() }
            .map { it.absolutePath }
            .toList()

        checkForInvalidElements(invalidAudioFiles, "audio files don't exist")
    }

    internal fun checkMissingPersonalities(
        messages: List<Message>,
        personalitiesMap: Map<String, String>
    ) {
        val invalidPersonalities = messages
            .flatMap { it.audioByPersonality.keys }
            .toSet()
            .filter { !personalitiesMap.containsKey(it) }

        checkForInvalidElements(invalidPersonalities, "personalities don't exist")
    }

    internal fun checkForInvalidElements(invalidElements: List<String>, prefix: String) {
        if (invalidElements.isNotEmpty()) {
            throw AudioImportException(
                invalidElements.joinToString(
                    separator = System.lineSeparator(),
                    prefix = "Audio import failed: the following $prefix\n"
                )
            )
        }
    }

    internal fun getPersonalitiesIdsMap(personalities: Set<Personality>) =
        personalities.map { personality -> personality.personalityId to personality.id }.toMap()
}

class AudioImportException(message: String) : Exception(message)
