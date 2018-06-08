package com.nuecho.genesys.cli.commands.audio.export

import com.fasterxml.jackson.core.type.TypeReference
import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.google.common.base.Charsets
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
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.getSchemaBuilder
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.getMissingPersonalitiesIds
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.getAudioMap
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.isSelectedPersonality
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.writeAudioData
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import io.kotlintest.matchers.beEmpty
import io.kotlintest.matchers.include
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.startWith
import io.kotlintest.specs.StringSpec
import io.kotlintest.matchers.shouldThrow
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.net.URL

private const val USAGE_PREFIX = "Usage: export [-?]"
private const val GAX_URL = "http://genesys.com"
private const val INTERNAL_SERVER_ERROR = 500

private const val FRANK_ID = "10"
private const val JOELLA_ID = "12"
private const val INVALID_PERSONALITY_ID = "15"

class AudioExportCommandTest : StringSpec() {
    init {
        val messagesData = File(getTestResource("commands/audio/messages_data.json").toURI())
        val personalitiesData = File(getTestResource("commands/audio/personalities_data.json").toURI())
        val audioCsv = File(getTestResource("commands/audio/audio_output.csv").toURI())
        val expectedAudioCsv = File(getTestResource("commands/audio/expected_audio_output.csv").toURI())

        val existingMessages: List<Message> = defaultJsonObjectMapper().readValue(messagesData.inputStream(), object : TypeReference<List<Message>>() {})
        val personalities: Set<Personality> = defaultJsonObjectMapper().readValue(personalitiesData.inputStream(), object : TypeReference<Set<Personality>>() {})

        // To disable the automatic redirection
        FuelManager.instance.removeAllResponseInterceptors()

        "executing Export with -h argument should print usage" {
            val output = CliOutputCaptureWrapper.execute("audio", "export", "-h")
            output should startWith(USAGE_PREFIX)
        }

        "executing Export with wrong argument should print usage" {
            val output = CliOutputCaptureWrapper.execute("audio", "export", "audioOutput.csv", "--with")
            output should include(USAGE_PREFIX)
        }

        "writeAudioData should fail when the response status code is wrong" {
            val schemaBuilder = getSchemaBuilder(personalities)

            mockHttpClient(
                INTERNAL_SERVER_ERROR,
                messagesData.inputStream()
            )
            shouldThrow<AudioServicesException> {
                writeAudioData(
                    getMessagesData(GAX_URL),
                    schemaBuilder,
                    audioCsv.outputStream()
                )
            }
        }

        "getMissingPersonalitiesIds should return personalities that don't exist" {
            getMissingPersonalitiesIds(personalities, setOf(INVALID_PERSONALITY_ID, FRANK_ID)) shouldBe setOf(INVALID_PERSONALITY_ID)
            getMissingPersonalitiesIds(personalities, null) should beEmpty()
        }

        "isSelectedPersonalities should return whether the personality is in the list or not" {
            isSelectedPersonality(FRANK_ID, setOf(FRANK_ID, JOELLA_ID)) shouldBe true
            isSelectedPersonality(JOELLA_ID, setOf(FRANK_ID, JOELLA_ID)) shouldBe true
            isSelectedPersonality(INVALID_PERSONALITY_ID, setOf(FRANK_ID, JOELLA_ID)) shouldBe false
            isSelectedPersonality(FRANK_ID, null) shouldBe true
        }

        "getAudioMap should return map including exclusively existing personality id keys" {
            val audioMap = getAudioMap(existingMessages, setOf(INVALID_PERSONALITY_ID, FRANK_ID), audioCsv.parentFile.path)

            audioMap shouldBe mapOf(
                "${audioCsv.parentFile.path}/$FRANK_ID/Test.wav" to AudioRequestInfo("10001", "10001"),
                "${audioCsv.parentFile.path}/$FRANK_ID/Enter Password.wav" to AudioRequestInfo("10002", "10003"),
                "${audioCsv.parentFile.path}/$FRANK_ID/foo.wav" to AudioRequestInfo("10003", "10004"),
                "${audioCsv.parentFile.path}/$FRANK_ID/BAZ.wav" to AudioRequestInfo("10004", "10005")
            )

            "getSchemaBuilder should properly build csv schema builder from personalities" {
                val schemaBuilder = getSchemaBuilder(personalities)

                schemaBuilder.hasColumn(NAME) shouldBe true
                schemaBuilder.hasColumn(DESCRIPTION) shouldBe true
                schemaBuilder.hasColumn(MESSAGE_AR_ID) shouldBe true
                schemaBuilder.hasColumn(TENANT_ID) shouldBe true
                schemaBuilder.hasColumn(FRANK_ID) shouldBe true
                schemaBuilder.hasColumn(JOELLA_ID) shouldBe true
            }

            "writeCsvFile should properly write in csv file" {
                writeAudioData(existingMessages, getSchemaBuilder(personalities), audioCsv.outputStream())

                audioCsv.readText(Charsets.UTF_8) shouldBe expectedAudioCsv.readText(Charsets.UTF_8)
            }
        }
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
