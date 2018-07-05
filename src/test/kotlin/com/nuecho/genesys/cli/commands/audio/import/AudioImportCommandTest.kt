package com.nuecho.genesys.cli.commands.audio.import

import com.fasterxml.jackson.core.type.TypeReference
import com.github.kittinunf.fuel.core.FuelManager
import com.nuecho.genesys.cli.CliOutputCaptureWrapper
import com.nuecho.genesys.cli.TestResources.getTestResource
import com.nuecho.genesys.cli.commands.audio.ArmMessage
import com.nuecho.genesys.cli.commands.audio.Message
import com.nuecho.genesys.cli.commands.audio.MessageType.ANNOUNCEMENT
import com.nuecho.genesys.cli.commands.audio.Personality
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.checkDuplicatedMessagesNames
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.checkForInvalidElements
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.checkMissingAudioFiles
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.checkMissingPersonalities
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.getPersonalityIdsMap
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.readAudioData
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.removeEmptyLines
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.io.ByteArrayInputStream
import java.io.File
import kotlin.text.Charsets.UTF_8

private const val USAGE_PREFIX = "Usage: import [-?]"

@TestInstance(PER_CLASS)
class AudioImportCommandTest {
    private val audioCsv = getTestResource("commands/audio/audios.csv")
    private val personalitiesData = File(getTestResource("commands/audio/personalities_data.json").toURI())
    private val personalities: Set<Personality> = defaultJsonObjectMapper().readValue(personalitiesData.inputStream(), object : TypeReference<Set<Personality>>() {})
    private val personalitiesMap = getPersonalityIdsMap(personalities)
    private val newMessages = readAudioData(audioCsv.openStream(), UTF_8)

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
    fun `readAudioData should properly deserialize audio messagesData`() {
        val audioData = readAudioData(audioCsv.openStream(), UTF_8)
        val expected = Message.Builder("foo", ANNOUNCEMENT, "fooscription")
            .withAudios(
                "12" to "foo.fr.wav",
                "14" to "foo.en.wav"
            ).build()

        assertThat(audioData.size, `is`(3))
        assertThat(audioData[0], equalTo(expected))
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
                    ArmMessage(
                        "foo",
                        ANNOUNCEMENT,
                        "",
                        "",
                        0,
                        0,
                        arrayListOf()
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
    fun `checkDuplicatedMessagesNames should throw AudioImportException when some message's names already exist`() {
        val messagesData = File(getTestResource("commands/audio/messages_data.json").toURI())
        val existingArmMessages: List<ArmMessage> = defaultJsonObjectMapper().readValue(messagesData.inputStream(), object : TypeReference<List<ArmMessage>>() {})

        assertThrows(AudioImportException::class.java) {
            checkDuplicatedMessagesNames(newMessages, existingArmMessages)
        }
    }

    @Test
    fun `getPersonalityIdsMap should properly map the personalities' ids to their personalityIds`() {
        assertThat(
            getPersonalityIdsMap(personalities),
            equalTo(
                mapOf(
                    "10" to "10002",
                    "12" to "10004"
                )
            )
        )
    }

    @Test
    fun `removeEmptyLines should skip blank lines`() {
        val charset = UTF_8
        val inputText = """line1
            |
            |
            |line2
            |
        """.trimMargin()
        val inputStream = ByteArrayInputStream(inputText.toByteArray(charset))

        assertThat(
            removeEmptyLines(inputStream, charset),
            equalTo(
                """line1
                  |line2
                """.trimMargin()
            )

        )
    }
}
