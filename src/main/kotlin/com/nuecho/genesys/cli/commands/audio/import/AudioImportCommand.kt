package com.nuecho.genesys.cli.commands.audio.import

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.core.FuelManager
import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.commands.GenesysCliCommand
import com.nuecho.genesys.cli.commands.audio.ArmMessage
import com.nuecho.genesys.cli.commands.audio.Audio
import com.nuecho.genesys.cli.commands.audio.AudioServices.DEFAULT_GAX_API_PATH
import com.nuecho.genesys.cli.commands.audio.AudioServices.buildGaxUrl
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
import java.nio.charset.Charset

@CommandLine.Command(
    name = "import",
    description = ["Import audio files in ARM"]
)
class AudioImportCommand : GenesysCliCommand() {
    @CommandLine.ParentCommand
    private var audio: Audio? = null

    @CommandLine.Option(
        names = ["--gax-api-path"],
        description = ["GAX API path. Defaults to '$DEFAULT_GAX_API_PATH'."]
    )
    private var gaxApiPath: String = DEFAULT_GAX_API_PATH

    @CommandLine.Option(
        names = ["--encoding"],
        description = ["Encoding of the input file. Defaults to UTF-8."]
    )
    private var encoding: Charset = Charsets.UTF_8

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
            getGenesysCli().loadEnvironment(password),
            inputFile!!.inputStream(),
            inputFile!!.absoluteFile.parentFile,
            encoding,
            gaxApiPath
        )

        return 0
    }
}

object AudioImport {

    fun importAudios(
        environment: Environment,
        audioData: InputStream,
        audioDirectory: File,
        encoding: Charset,
        gaxApiPath: String
    ) {
        // To disable the automatic redirection
        FuelManager.instance.removeAllResponseInterceptors()
        CookieHandler.setDefault(CookieManager())

        val gaxUrl = buildGaxUrl(environment, gaxApiPath)
        var callbackSequenceNumber = 1
        val messages = try {
            readAudioData(audioData, encoding)
        } catch (exception: JsonMappingException) {
            throw AudioImportException("Invalid input file.", exception)
        }

        info { "Logging in to GAX as '${environment.user}'." }
        login(environment.user, environment.password!!.value, true, gaxUrl)

        val existingMessages = getMessagesData(gaxUrl)
        checkDuplicatedMessagesNames(messages, existingMessages)

        val personalityIdToQueryIdMap = getPersonalityIdsMap(getPersonalities(gaxUrl))
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

        println("Imported ${messages.size} message${if (messages.size > 1) "s" else ""}.")
    }

    internal fun readAudioData(inputStream: InputStream, encoding: Charset) =
        CsvMapper()
            .registerKotlinModule()
            .readerFor(Message::class.java)
            .with(CsvSchema.emptySchema().withHeader())
            .readValues<Message>(removeEmptyLines(inputStream, encoding))
            .readAll()

    // Ideally we would like our csv mapper to use .withFeatures(CsvParser.Feature.SKIP_EMPTY_LINES) but the
    // feature is not yet implemented https://github.com/FasterXML/jackson-dataformats-text/issues/15
    internal fun removeEmptyLines(inputStream: InputStream, encoding: Charset) =
        inputStream
            .reader(encoding)
            .readLines()
            .filter { it.isNotBlank() }
            .joinToString(separator = "\n")

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

    internal fun getPersonalityIdsMap(personalities: Set<Personality>) =
        personalities.map { personality -> personality.personalityId to personality.id }.toMap()
}

class AudioImportException(message: String, cause: Throwable? = null) : Exception(message, cause)
