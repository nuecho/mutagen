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

package com.nuecho.mutagen.cli.commands.audio

import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.nuecho.mutagen.cli.TestResources.getTestResource
import com.nuecho.mutagen.cli.commands.audio.AudioServices.APPLICATION_JSON
import com.nuecho.mutagen.cli.commands.audio.AudioServices.AUDIO_MESSAGES_PATH
import com.nuecho.mutagen.cli.commands.audio.AudioServices.AUDIO_PATH
import com.nuecho.mutagen.cli.commands.audio.AudioServices.AUDIO_RESOURCES_PATH
import com.nuecho.mutagen.cli.commands.audio.AudioServices.CONTENT_TYPE
import com.nuecho.mutagen.cli.commands.audio.AudioServices.CREATE_MESSAGE_SUCCESS_CODE
import com.nuecho.mutagen.cli.commands.audio.AudioServices.FILES_PATH
import com.nuecho.mutagen.cli.commands.audio.AudioServices.LOCATION
import com.nuecho.mutagen.cli.commands.audio.AudioServices.LOGIN_PATH
import com.nuecho.mutagen.cli.commands.audio.AudioServices.LOGIN_SUCCESS_CODE
import com.nuecho.mutagen.cli.commands.audio.AudioServices.PERSONALITIES_PATH
import com.nuecho.mutagen.cli.commands.audio.AudioServices.SUCCESS_CODE
import com.nuecho.mutagen.cli.commands.audio.AudioServices.UPLOAD_PATH
import com.nuecho.mutagen.cli.commands.audio.AudioServices.createMessage
import com.nuecho.mutagen.cli.commands.audio.AudioServices.downloadAudioFile
import com.nuecho.mutagen.cli.commands.audio.AudioServices.getMessagesData
import com.nuecho.mutagen.cli.commands.audio.AudioServices.getPersonalities
import com.nuecho.mutagen.cli.commands.audio.AudioServices.login
import com.nuecho.mutagen.cli.commands.audio.AudioServices.uploadAudio
import com.nuecho.mutagen.cli.commands.audio.MessageType.ANNOUNCEMENT
import com.nuecho.mutagen.cli.commands.audio.export.AudioExport.buildCsvSchema
import com.nuecho.mutagen.cli.commands.audio.export.AudioExport.writeCsv
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.URL

private const val GAX_URL = "http://genesys.com"
private const val MESSAGE_URL = "$GAX_URL/messages/1234"
private const val INTERNAL_SERVER_ERROR = 500

class AudioServicesTest {
    private val personalitiesData = File(getTestResource("commands/audio/personalities_data.json").toURI())
    private val messagesData = File(getTestResource("commands/audio/messages_data.json").toURI())
    private val parentFile = File(getTestResource("commands/audio").toURI())
    private val audioWav = File(getTestResource("commands/audio/audio.wav").toURI())

    @Test
    fun `uploadAudio should properly build the Request`() {
        val personality = "12345"
        val callbackSequenceNumber = 1

        mockHttpClient(SUCCESS_CODE) {
            assertThat(it.method, equalTo(Method.POST))
            assertThat(it.body(), containsString(personality))
            assertThat(it.path, startsWith("$MESSAGE_URL$UPLOAD_PATH"))
            assertThat(it.path, endsWith(callbackSequenceNumber.toString()))
        }

        uploadAudio(MESSAGE_URL, audioWav, personality, callbackSequenceNumber)
    }

    @Test
    fun `uploadAudio should properly handle error`() {
        val personality = "12345"
        val callbackSequenceNumber = 1

        mockHttpClient(666)

        assertThrows(AudioServicesException::class.java) {
            uploadAudio(MESSAGE_URL, audioWav, personality, callbackSequenceNumber)
        }
    }

    @Test
    fun `login should properly handle success`() {
        val user = "user"
        val password = "password"

        mockHttpClient(LOGIN_SUCCESS_CODE) {
            assertThat(it.method, equalTo(Method.POST))
            assertThat(it.body(), equalTo("""{"username":"$user","password":"$password","isPasswordEncrypted":false}"""))
            assertThat(it.headers[CONTENT_TYPE], equalTo(APPLICATION_JSON))
            assertThat(it.path, equalTo("$GAX_URL$LOGIN_PATH"))
        }

        login(user, password, false, GAX_URL)
    }

    @Test
    fun `login should properly handle error`() {
        mockHttpClient(666)
        assertThrows(AudioServicesException::class.java) {
            login("user", "password", false, GAX_URL)
        }
    }

    @Test
    fun `writeAudioData should properly build the Request`() {
        val personalities = setOf("10", "12", "15")

        mockHttpClient(
            SUCCESS_CODE,
            messagesData.inputStream()
        ) {
            assertThat(it.method, equalTo(Method.GET))
            assertThat(it.headers[CONTENT_TYPE], equalTo(APPLICATION_JSON))
            assertThat(it.path, equalTo("$GAX_URL$AUDIO_MESSAGES_PATH"))
        }

        writeCsv(
            getMessagesData(GAX_URL),
            buildCsvSchema(personalities),
            ByteArrayOutputStream()
        )
    }

    @Test
    fun `downloadAudioFile should properly build the request`() {
        mockHttpClient(SUCCESS_CODE) {
            assertThat(it.method, equalTo(Method.GET))
            assertThat(it.path, equalTo("$GAX_URL${AudioServices.AUDIO_RESOURCES_PATH}/10001$FILES_PATH/10001$AUDIO_PATH"))
        }

        val file = File("${parentFile.path}test.wav")
        file.createNewFile()

        downloadAudioFile(
            GAX_URL,
            AudioRequestInfo("10001", "10001"),
            file
        )

        file.delete()
    }

    @Test
    fun `getPersonalities should fail when the response status code is wrong`() {
        mockHttpClient(
            INTERNAL_SERVER_ERROR,
            ByteArrayInputStream("".toByteArray())
        )
        assertThrows(AudioServicesException::class.java) {
            getPersonalities(GAX_URL)
        }
    }

    @Test
    fun `getPersonalities should properly build the Request`() {
        mockHttpClient(
            SUCCESS_CODE, personalitiesData.inputStream()
        ) {
            assertThat(it.method, equalTo(Method.GET))
            assertThat(it.headers[CONTENT_TYPE], equalTo(APPLICATION_JSON))
            assertThat(it.path, equalTo("$GAX_URL$PERSONALITIES_PATH"))
        }

        getPersonalities(GAX_URL)
    }

    @Test
    fun `getMessagesData should fail when the response status code is wrong`() {
        mockHttpClient(
            INTERNAL_SERVER_ERROR,
            ByteArrayInputStream("".toByteArray())
        )
        assertThrows(AudioServicesException::class.java) {
            getMessagesData(GAX_URL)
        }
    }

    @Test
    fun `getMessagesData should properly build the Request`() {
        mockHttpClient(
            SUCCESS_CODE, messagesData.inputStream()
        ) {
            assertThat(it.method, equalTo(Method.GET))
            assertThat(it.headers[CONTENT_TYPE], equalTo(APPLICATION_JSON))
            assertThat(it.path, equalTo("$GAX_URL$AUDIO_MESSAGES_PATH"))
        }

        getMessagesData(GAX_URL)
    }

    @Test
    fun `createMessage should return the message url`() {
        val name = "name"
        val description = "description"

        mockHttpClient(
            statusCode = CREATE_MESSAGE_SUCCESS_CODE,
            headers = mapOf(LOCATION to listOf(MESSAGE_URL))
        ) {
            assertThat(it.method, equalTo(Method.POST))
            assertThat(it.body(), equalTo("""{"name":"$name","description":"$description","type":"ANNOUNCEMENT","privateResource":false}"""))
            assertThat(it.headers[CONTENT_TYPE], equalTo(APPLICATION_JSON))
            assertThat(it.path, equalTo("$GAX_URL$AUDIO_RESOURCES_PATH"))
        }

        val messageUrl = createMessage("name", ANNOUNCEMENT, "description", GAX_URL)
        assertThat(messageUrl, equalTo(MESSAGE_URL))
    }

    @Test
    fun `createMessage should fail if message url is missing`() {
        mockHttpClient(CREATE_MESSAGE_SUCCESS_CODE)
        assertThrows(AudioServicesException::class.java) {
            createMessage("name", ANNOUNCEMENT, "description", GAX_URL)
        }
    }

    @Test
    fun `createMessage should properly handle error`() {
        mockHttpClient(statusCode = 666, headers = mapOf(LOCATION to listOf(MESSAGE_URL)))
        assertThrows(AudioServicesException::class.java) {
            createMessage("name", ANNOUNCEMENT, "description", GAX_URL)
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
    // Disabling auto-redirect
    FuelManager.instance.removeAllResponseInterceptors()
}

private fun Request.body() = ByteArrayOutputStream().also {
    bodyCallback?.invoke(this, it, 0)
}.toString()
