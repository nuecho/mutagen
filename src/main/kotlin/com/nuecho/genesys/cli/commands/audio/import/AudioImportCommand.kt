package com.nuecho.genesys.cli.commands.audio.import

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.github.kittinunf.fuel.core.FuelManager
import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.commands.audio.Audio
import com.nuecho.genesys.cli.commands.audio.AudioServices.DESCRIPTION
import com.nuecho.genesys.cli.commands.audio.AudioServices.HTTP
import com.nuecho.genesys.cli.commands.audio.AudioServices.MESSAGE_AR_ID
import com.nuecho.genesys.cli.commands.audio.AudioServices.NAME
import com.nuecho.genesys.cli.commands.audio.AudioServices.TENANT_ID
import com.nuecho.genesys.cli.commands.audio.AudioServices.createMessage
import com.nuecho.genesys.cli.commands.audio.AudioServices.getMessagesData
import com.nuecho.genesys.cli.commands.audio.AudioServices.getPersonalities
import com.nuecho.genesys.cli.commands.audio.AudioServices.login
import com.nuecho.genesys.cli.commands.audio.AudioServices.uploadAudio
import com.nuecho.genesys.cli.commands.audio.Message
import com.nuecho.genesys.cli.commands.audio.Personality
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

    override fun execute() {
        AudioImport.importAudios(
            getGenesysCli().loadEnvironment(),
            inputFile!!.inputStream(),
            inputFile!!.absoluteFile.parentFile
        )
    }
}

object AudioImport {

    fun importAudios(environment: Environment, audioData: InputStream, audioDirectory: File) {
        // To disable the automatic redirection
        FuelManager.instance.removeAllResponseInterceptors()
        CookieHandler.setDefault(CookieManager())

        val gaxUrl = "$HTTP${environment.host}:${environment.port}"
        var callbackSequenceNumber = 1
        val messages = readAudioData(audioData).readAll()

        info { "Logging in to GAX as '${environment.user}'." }
        login(environment.user, environment.password!!, true, gaxUrl)

        val existingMessages = getMessagesData(gaxUrl)
        checkDuplicatedMessagesNames(messages, existingMessages)

        val personalityIdToQueryIdMap = getPersonalitiesIdsMap(getPersonalities(gaxUrl))
        checkMissingPersonalities(messages, personalityIdToQueryIdMap)

        checkMissingAudioFiles(messages, audioDirectory)

        messages.forEach {
            val message = it.toMutableMap()
            val name = message.remove(NAME) ?: throw AudioImportException("Missing $NAME column in audio data file")
            val description = message.remove(DESCRIPTION) ?: ""

            info { "Creating message '$name'." }
            val messageUrl = createMessage(name, description, gaxUrl)
            val url = "${messageUrl.substringBeforeLast("/")}/audioresources/${messageUrl.substringAfterLast("/")}"

            message.filter { (column, value) -> isPersonality(column) && !value.isEmpty() }
                .forEach { (personality, audioPath) ->
                    info { "Uploading '$audioPath'." }
                    uploadAudio(
                        messageUrl = url,
                        audioFile = File(audioDirectory, audioPath),
                        personality = personalityIdToQueryIdMap.get(personality)!!,
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

    internal fun checkDuplicatedMessagesNames(
        messages: List<Map<String, String>>,
        existingMessages: List<Message>
    ) {
        val invalidMessagesNames = findExistingMessagesNames(messages, existingMessages)
        checkForInvalidElements(invalidMessagesNames, "messages' names already exist on the gax server")
    }

    internal fun findExistingMessagesNames(
        newMessages: List<Map<String, String>>,
        messagesData: List<Message>
    ): List<String> {
        val messagesNames = newMessages.map { it.get(NAME)?.toLowerCase() ?: "" }.toMutableList()
        messagesNames.retainAll(messagesData.map { it.name.toLowerCase() })

        return messagesNames
    }

    internal fun checkMissingAudioFiles(messages: List<Map<String, String>>, audioDirectory: File) {
        val invalidAudioFiles = findMissingAudioFiles(messages, audioDirectory.absolutePath)
        checkForInvalidElements(invalidAudioFiles, "audio files don't exist")
    }

    internal fun findMissingAudioFiles(newMessages: List<Map<String, String>>, audioDirectory: String) =
        newMessages.flatMap { it.entries }
            .filter { (column, value) -> isPersonality(column) && !value.isEmpty() }
            .map { (_, value) -> File(audioDirectory, value) }
            .filter { !it.exists() }
            .map { it.absolutePath }
            .toList()

    internal fun checkMissingPersonalities(
        messages: List<Map<String, String>>,
        personalitiesMap: Map<String, String>
    ) {
        val invalidPersonalities = getMissingPersonalities(messages, personalitiesMap)
        checkForInvalidElements(invalidPersonalities, "personalities don't exist")
    }

    internal fun getMissingPersonalities(messages: List<Map<String, String>>, personalitiesMap: Map<String, String>) =
    // all messages have the same keys, so only one message's personalities actually have to be checked
        messages[0].keys.filter { key -> isPersonality(key) && !personalitiesMap.containsKey(key) }

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

    private fun isPersonality(key: String) =
        key != MESSAGE_AR_ID && key != TENANT_ID && key != NAME && key != DESCRIPTION
}

class AudioImportException(message: String) : Exception(message)
