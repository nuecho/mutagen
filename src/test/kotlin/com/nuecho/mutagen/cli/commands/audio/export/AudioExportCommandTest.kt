/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nuecho.mutagen.cli.commands.audio.export

import com.fasterxml.jackson.core.type.TypeReference
import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.nuecho.mutagen.cli.CliOutputCaptureWrapper
import com.nuecho.mutagen.cli.TestResources.getTestResource
import com.nuecho.mutagen.cli.commands.audio.ArmMessage
import com.nuecho.mutagen.cli.commands.audio.AudioRequestInfo
import com.nuecho.mutagen.cli.commands.audio.AudioServices.getMessagesData
import com.nuecho.mutagen.cli.commands.audio.AudioServicesException
import com.nuecho.mutagen.cli.commands.audio.Personality
import com.nuecho.mutagen.cli.commands.audio.export.AudioExport.buildCsvSchema
import com.nuecho.mutagen.cli.commands.audio.export.AudioExport.getAudioMap
import com.nuecho.mutagen.cli.commands.audio.export.AudioExport.getMissingPersonalityIds
import com.nuecho.mutagen.cli.commands.audio.export.AudioExport.isSelectedPersonality
import com.nuecho.mutagen.cli.commands.audio.export.AudioExport.writeCsv
import com.nuecho.mutagen.cli.core.defaultJsonObjectMapper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
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
    private val messagesData = File(getTestResource("commands/audio/messages_data.json").toURI())
    private val personalitiesData = File(getTestResource("commands/audio/personalities_data.json").toURI())
    private val parentFile = File(getTestResource("commands/audio").toURI())
    private val expectedAudioCsv = File(getTestResource("commands/audio/expected_audio_output.csv").toURI())

    private val existingArmMessages: List<ArmMessage> = defaultJsonObjectMapper().readValue(messagesData.inputStream(), object : TypeReference<List<ArmMessage>>() {})
    private val personalities: Set<Personality> = defaultJsonObjectMapper().readValue(personalitiesData.inputStream(), object : TypeReference<Set<Personality>>() {})
    private val personalityIds = personalities.map { it.personalityId }.toSortedSet()

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
        val schema = buildCsvSchema(personalityIds)

        mockHttpClient(
            INTERNAL_SERVER_ERROR,
            messagesData.inputStream()
        )
        assertThrows(AudioServicesException::class.java) {
            writeCsv(
                getMessagesData(GAX_URL, emptySet()),
                schema,
                ByteArrayOutputStream()
            )
        }
    }

    @Test
    fun `getMissingPersonalityIds should return personalities that don't exist`() {
        assertThat(getMissingPersonalityIds(personalities, setOf(INVALID_PERSONALITY_ID, FRANK_ID)), equalTo(setOf(INVALID_PERSONALITY_ID)))
        assertThat(getMissingPersonalityIds(personalities, null), `is`(empty()))
    }

    @Test
    fun `isSelectedPersonalities should return whether the personality is in the list or not`() {
        assertThat(isSelectedPersonality(FRANK_ID, setOf(FRANK_ID, JOELLA_ID)), `is`(true))
        assertThat(isSelectedPersonality(JOELLA_ID, setOf(FRANK_ID, JOELLA_ID)), `is`(true))
        assertThat(isSelectedPersonality(INVALID_PERSONALITY_ID, setOf(FRANK_ID, JOELLA_ID)), `is`(false))
        assertThat(isSelectedPersonality(FRANK_ID, emptySet()), `is`(true))
    }

    @Test
    fun `getAudioMap should return map including exclusively existing personality id keys`() {
        val audioMap = getAudioMap(existingArmMessages, setOf(INVALID_PERSONALITY_ID, FRANK_ID), parentFile.path)

        assertThat(
            audioMap,
            equalTo(
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
    fun `buildCsvSchema should properly build csv schema from personalities`() {
        val schema = buildCsvSchema(personalityIds)

        assertThat(schema.column("name"), notNullValue())
        assertThat(schema.column("name"), notNullValue())
        assertThat(schema.column("description"), notNullValue())
        assertThat(schema.column("messageArId"), notNullValue())
        assertThat(schema.column("tenantId"), notNullValue())
        assertThat(schema.column(FRANK_ID), notNullValue())
        assertThat(schema.column(JOELLA_ID), notNullValue())
    }

    @Test
    fun `writeCsvFile should properly write in csv file`() {
        val output = ByteArrayOutputStream()
        writeCsv(existingArmMessages, buildCsvSchema(personalityIds), output)
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
