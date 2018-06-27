package com.nuecho.genesys.cli.commands.audio.import

import com.fasterxml.jackson.core.type.TypeReference
import com.github.kittinunf.fuel.core.FuelManager
import com.nuecho.genesys.cli.CliOutputCaptureWrapper
import com.nuecho.genesys.cli.TestResources.getTestResource
import com.nuecho.genesys.cli.commands.audio.AudioServicesException
import com.nuecho.genesys.cli.commands.audio.Message
import com.nuecho.genesys.cli.commands.audio.Personality
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.checkDuplicatedMessagesNames
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.checkForInvalidElements
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.checkMissingAudioFiles
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.checkMissingPersonalities
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.findExistingMessagesNames
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.findMissingAudioFiles
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.getMissingPersonalities
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.getPersonalitiesIdsMap
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.importAudios
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.readAudioData
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.preferences.environment.Environment
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.io.File

private const val USAGE_PREFIX = "Usage: import [-?]"

@TestInstance(PER_CLASS)
class AudioImportCommandTest {
    private val audioCsv = getTestResource("commands/audio/audios.csv")

    private val personalitiesData = File(getTestResource("commands/audio/personalities_data.json").toURI())
    private val personalities: Set<Personality> = defaultJsonObjectMapper().readValue(personalitiesData.inputStream(), object : TypeReference<Set<Personality>>() {})
    private val personalitiesMap = getPersonalitiesIdsMap(personalities)
    private val newMessages = readAudioData(audioCsv.openStream()).readAll()

    @BeforeAll
    fun init() {
        // To disable the automatic redirection
        FuelManager.instance.removeAllResponseInterceptors()
    }

    @Test
    fun `executing Import with -h argument should print usage`() {
        val output = CliOutputCaptureWrapper.execute("audio", "import", "-h")
        assertThat(output, startsWith(USAGE_PREFIX))
    }

    @Test
    fun `importAudios should fail when name column is missing`() {
        assertThrows(AudioServicesException::class.java) {
            importAudios(
                Environment(host = "host", user = "user", rawPassword = "password"),
                audioCsv.openStream(),
                File("audioDirectory")
            )
        }
    }

    @Test
    fun `readAudioData should properly deserialize audio messagesData`() {
        val audioData = readAudioData(audioCsv.openStream())
        assertThat(
            audioData.next(),
            equalTo(
                mapOf(
                    "name" to "foo",
                    "description" to "fooscription",
                    "12" to "foo.fr.wav",
                    "14" to "foo.en.wav",
                    "10" to ""
                )
            )
        )

        audioData.next()
        audioData.next()
        assertThat(audioData.hasNextValue(), `is`(false))
    }

    @Test
    fun `warnOfInvalidElements should return whether the list of invalid elements is empty or not`() {
        assertThrows(AudioImportException::class.java) {
            checkForInvalidElements(listOf("test"), "")
        }
    }

    @Test
    fun `checkMissingPersonalities should throw AudioImportException when there is a missing perosnality`() {
        assertThrows(AudioImportException::class.java) {
            checkMissingPersonalities(newMessages, personalitiesMap)
        }
    }

    @Test
    fun `checkDuplicatedMessagesNames should throw AudioImportException when there are duplicated messages`() {
        assertThrows(AudioImportException::class.java) {
            checkDuplicatedMessagesNames(
                newMessages, listOf(
                    Message(
                        "foo",
                        "",
                        "",
                        0,
                        0,
                        ArrayList()
                    )
                )
            )
        }
    }

    @Test
    fun `checkMissingAudioFiles should throw AudioImportException when there are files that don't exist`() {
        assertThrows(AudioImportException::class.java) {
            checkMissingAudioFiles(newMessages, File(audioCsv.toURI()).parentFile)
        }
    }

    @Test
    fun `findMissingAudioFiles should only return files that don't exist`() {
        val audioDirectoryPath = File(audioCsv.toURI()).parent

        assertThat(
            findMissingAudioFiles(newMessages, audioDirectoryPath), equalTo(
                listOf(
                    File(audioDirectoryPath, "foo.fr.wav").absolutePath,
                    File(audioDirectoryPath, "foo.en.wav").absolutePath,
                    File(audioDirectoryPath, "bar.fr.wav").absolutePath,
                    File(audioDirectoryPath, "baz.fr.wav").absolutePath,
                    File(audioDirectoryPath, "baz.en.wav").absolutePath
                )
            )
        )
    }

    @Test
    fun `findExistingMessagesNames should return file names that already exist, without case sensitivity`() {
        val messagesData = File(getTestResource("commands/audio/messages_data.json").toURI())
        val existingMessages: List<Message> = defaultJsonObjectMapper().readValue(messagesData.inputStream(), object : TypeReference<List<Message>>() {})

        assertThat(findExistingMessagesNames(newMessages, existingMessages), contains("foo", "baz"))
    }

    @Test
    fun `getPersonalitiesIdsMap should properly map the personalities' ids to their personalityIds`() {
        assertThat(
            getPersonalitiesIdsMap(personalities),
            equalTo(
                mapOf(
                    "10" to "10002",
                    "12" to "10004"
                )
            )
        )
    }

    @Test
    fun `getMissingPersonalities should return personalities that don't exist`() {
        assertThat(getMissingPersonalities(newMessages, personalitiesMap), equalTo(listOf("14")))
    }
}
