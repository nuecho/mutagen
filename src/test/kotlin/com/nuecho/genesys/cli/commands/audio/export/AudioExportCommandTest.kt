package com.nuecho.genesys.cli.commands.audio.export

import com.fasterxml.jackson.core.type.TypeReference
import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.nuecho.genesys.cli.CliOutputCaptureWrapper
import com.nuecho.genesys.cli.TestResources.getTestResource
import com.nuecho.genesys.cli.commands.audio.AudioRequestInfo
import com.nuecho.genesys.cli.commands.audio.AudioServices.DESCRIPTION
import com.nuecho.genesys.cli.commands.audio.AudioServices.MESSAGE_AR_ID
import com.nuecho.genesys.cli.commands.audio.AudioServices.NAME
import com.nuecho.genesys.cli.commands.audio.AudioServices.TENANT_ID
import com.nuecho.genesys.cli.commands.audio.AudioServices.getMessagesData
import com.nuecho.genesys.cli.commands.audio.AudioServicesException
import com.nuecho.genesys.cli.commands.audio.Message
import com.nuecho.genesys.cli.commands.audio.Personality
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.getAudioMap
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.getMissingPersonalitiesIds
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.getSchemaBuilder
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.isSelectedPersonality
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.writeAudioData
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.URL

private const val USAGE_PREFIX = "Usage: export [-?]"
private const val GAX_URL = "http://genesys.com"
private const val INTERNAL_SERVER_ERROR = 500

private const val FRANK_ID = "10"
private const val JOELLA_ID = "12"
private const val INVALID_PERSONALITY_ID = "15"

@TestInstance(PER_CLASS)
class AudioExportCommandTest {
    val messagesData = File(getTestResource("commands/audio/messages_data.json").toURI())
    val personalitiesData = File(getTestResource("commands/audio/personalities_data.json").toURI())
    val parentFile = File(getTestResource("commands/audio").toURI())
    val expectedAudioCsv = File(getTestResource("commands/audio/expected_audio_output.csv").toURI())

    val existingMessages: List<Message> = defaultJsonObjectMapper().readValue(messagesData.inputStream(), object : TypeReference<List<Message>>() {})
    val personalities: Set<Personality> = defaultJsonObjectMapper().readValue(personalitiesData.inputStream(), object : TypeReference<Set<Personality>>() {})

    @BeforeAll
    fun init() {
        // To disable the automatic redirection
        FuelManager.instance.removeAllResponseInterceptors()
    }

    @Test
    fun `executing Export with -h argument should print usage`() {
        val output = CliOutputCaptureWrapper.execute("audio", "export", "-h")
        assertThat(output, startsWith(USAGE_PREFIX))
    }

    @Test
    fun `executing Export with wrong argument should print usage`() {
        val output = CliOutputCaptureWrapper.execute("audio", "export", "audioOutput.csv", "--with")
        assertThat(output, containsString(USAGE_PREFIX))
    }

    @Test
    fun `writeAudioData should fail when the response status code is wrong`() {
        val schemaBuilder = getSchemaBuilder(personalities)

        mockHttpClient(
            INTERNAL_SERVER_ERROR,
            messagesData.inputStream()
        )
        assertThrows(AudioServicesException::class.java) {
            writeAudioData(
                getMessagesData(GAX_URL),
                schemaBuilder,
                ByteArrayOutputStream()
            )
        }
    }

    @Test
    fun `getMissingPersonalitiesIds should return personalities that don't exist`() {
        assertThat(getMissingPersonalitiesIds(personalities, setOf(INVALID_PERSONALITY_ID, FRANK_ID)), equalTo(setOf(INVALID_PERSONALITY_ID)))
        assertThat(getMissingPersonalitiesIds(personalities, null), `is`(empty()))
    }

    @Test
    fun `isSelectedPersonalities should return whether the personality is in the list or not`() {
        assertThat(isSelectedPersonality(FRANK_ID, setOf(FRANK_ID, JOELLA_ID)), `is`(true))
        assertThat(isSelectedPersonality(JOELLA_ID, setOf(FRANK_ID, JOELLA_ID)), `is`(true))
        assertThat(isSelectedPersonality(INVALID_PERSONALITY_ID, setOf(FRANK_ID, JOELLA_ID)), `is`(false))
        assertThat(isSelectedPersonality(FRANK_ID, null), `is`(true))
    }

    @Test
    fun `getAudioMap should return map including exclusively existing personality id keys`() {
        val audioMap = getAudioMap(existingMessages, setOf(INVALID_PERSONALITY_ID, FRANK_ID), parentFile.path)

        assertThat(
            audioMap, equalTo(
                mapOf(
                    "${parentFile.path}/$FRANK_ID/Test.wav" to AudioRequestInfo("10001", "10001"),
                    "${parentFile.path}/$FRANK_ID/Enter Password.wav" to AudioRequestInfo("10002", "10003"),
                    "${parentFile.path}/$FRANK_ID/foo.wav" to AudioRequestInfo("10003", "10004"),
                    "${parentFile.path}/$FRANK_ID/BAZ.wav" to AudioRequestInfo("10004", "10005")
                )
            )
        )
    }

    @Test
    fun `getSchemaBuilder should properly build csv schema builder from personalities`() {
        val schemaBuilder = getSchemaBuilder(personalities)

        assertThat(schemaBuilder.hasColumn(NAME), `is`(true))
        assertThat(schemaBuilder.hasColumn(DESCRIPTION), `is`(true))
        assertThat(schemaBuilder.hasColumn(MESSAGE_AR_ID), `is`(true))
        assertThat(schemaBuilder.hasColumn(TENANT_ID), `is`(true))
        assertThat(schemaBuilder.hasColumn(FRANK_ID), `is`(true))
        assertThat(schemaBuilder.hasColumn(JOELLA_ID), `is`(true))
    }

    @Test
    fun `writeCsvFile should properly write in csv file`() {
        val output = ByteArrayOutputStream()
        writeAudioData(existingMessages, getSchemaBuilder(personalities), output)
        assertThat(output.toString(), equalTo(expectedAudioCsv.readText()))
    }
}

private fun mockHttpClient(
    statusCode: Int,
    data: InputStream = ByteArrayInputStream(ByteArray(0)),
    headers: Map<String, List<String>> = emptyMap(),
    validateRequest: (request: Request) -> Unit = {}
) {
    val client = object : Client {
        override fun executeRequest(request: Request): Response {

            validateRequest(request)
            return Response(
                url = URL(GAX_URL),
                statusCode = statusCode,
                headers = headers,
                dataStream = data
            )
        }
    }

    FuelManager.instance.client = client
}
