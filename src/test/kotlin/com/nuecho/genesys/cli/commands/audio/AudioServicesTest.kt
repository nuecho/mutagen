package com.nuecho.genesys.cli.commands.audio

import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.nuecho.genesys.cli.TestResources.getTestResource
import com.nuecho.genesys.cli.commands.audio.AudioServices.APPLICATION_JSON
import com.nuecho.genesys.cli.commands.audio.AudioServices.AUDIO_MESSAGES_PATH
import com.nuecho.genesys.cli.commands.audio.AudioServices.AUDIO_PATH
import com.nuecho.genesys.cli.commands.audio.AudioServices.AUDIO_RESOURCES_PATH
import com.nuecho.genesys.cli.commands.audio.AudioServices.CONTENT_TYPE
import com.nuecho.genesys.cli.commands.audio.AudioServices.CREATE_MESSAGE_SUCCESS_CODE
import com.nuecho.genesys.cli.commands.audio.AudioServices.FILES_PATH
import com.nuecho.genesys.cli.commands.audio.AudioServices.LOCATION
import com.nuecho.genesys.cli.commands.audio.AudioServices.LOGIN_PATH
import com.nuecho.genesys.cli.commands.audio.AudioServices.LOGIN_SUCCESS_CODE
import com.nuecho.genesys.cli.commands.audio.AudioServices.PERSONALITIES_PATH
import com.nuecho.genesys.cli.commands.audio.AudioServices.getPersonalities
import com.nuecho.genesys.cli.commands.audio.AudioServices.login
import com.nuecho.genesys.cli.commands.audio.AudioServices.SUCCESS_CODE
import com.nuecho.genesys.cli.commands.audio.AudioServices.UPLOAD_PATH
import com.nuecho.genesys.cli.commands.audio.AudioServices.createMessage
import com.nuecho.genesys.cli.commands.audio.AudioServices.downloadAudioFile
import com.nuecho.genesys.cli.commands.audio.AudioServices.getMessagesData
import com.nuecho.genesys.cli.commands.audio.AudioServices.uploadAudio
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.downloadAudioFiles
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.writeAudioData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
private const val FRANK_ID = "10"

class AudioServicesTest {
    val personalitiesData = File(getTestResource("commands/audio/personalities_data.json").toURI())
    val messagesData = File(getTestResource("commands/audio/messages_data.json").toURI())
    val parentFile = File(getTestResource("commands/audio").toURI())
    val audioWav = File(getTestResource("commands/audio/audio.wav").toURI())

    @Test
    fun `uploadAudio should properly build the Request`() {
        val personality = "12345"
        val callbackSequenceNumber = 1

        mockHttpClient(SUCCESS_CODE) {
            assertEquals(Method.POST, it.method)
            assertTrue(it.body().contains(personality))
            assertTrue(it.path.startsWith("$MESSAGE_URL$UPLOAD_PATH"))
            assertTrue(it.path.endsWith(callbackSequenceNumber.toString()))
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
            assertEquals(Method.POST, it.method)
            assertEquals("""{"username":"$user","password":"$password","isPasswordEncrypted":false}""", it.body())
            assertEquals(APPLICATION_JSON, it.headers[CONTENT_TYPE])
            assertEquals("$GAX_URL$LOGIN_PATH", it.path)
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
        val schemaBuilder = CsvSchema.Builder()
            .addColumn(AudioServices.NAME)
            .addColumn(AudioServices.DESCRIPTION)
            .addColumn(AudioServices.TENANT_ID)
            .addColumn(AudioServices.MESSAGE_AR_ID)
            .addColumn("10")
            .addColumn("12")
            .addColumn("15")

        mockHttpClient(
            SUCCESS_CODE,
            messagesData.inputStream()
        ) {
            assertEquals(Method.GET, it.method)
            assertEquals(APPLICATION_JSON, it.headers[CONTENT_TYPE])
            assertEquals("$GAX_URL$AUDIO_MESSAGES_PATH", it.path)
        }

        writeAudioData(
            getMessagesData(GAX_URL),
            schemaBuilder,
            ByteArrayOutputStream()
        )
    }

    @Test
    fun `downloadAudioFile should properly build the request`() {
        mockHttpClient(SUCCESS_CODE) {
            assertEquals(Method.GET, it.method)
            assertEquals("$GAX_URL${AudioServices.AUDIO_RESOURCES_PATH}/10001$FILES_PATH/10001$AUDIO_PATH", it.path)
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
    fun `downloadAudioFiles should properly write in audio files`() {
        val audioMap = mapOf(
            "${parentFile.path}/$FRANK_ID/Test.wav" to AudioRequestInfo("10001", "10001"),
            "${parentFile.path}/$FRANK_ID/Enter Password.wav" to AudioRequestInfo("10002", "10003"),
            "${parentFile.path}/$FRANK_ID/foo.wav" to AudioRequestInfo("10003", "10004"),
            "${parentFile.path}/$FRANK_ID/BAZ.wav" to AudioRequestInfo("10004", "10005")
        )

        mockHttpClient(
            SUCCESS_CODE,
            audioWav.inputStream()
        )

        downloadAudioFiles(
            GAX_URL,
            audioMap
        )

        audioMap.forEach { filePath, _ ->
            assertEquals(File(filePath).readText(Charsets.UTF_8), audioWav.readText(Charsets.UTF_8))
        }
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
            assertEquals(it.method, Method.GET)
            assertEquals(it.headers[CONTENT_TYPE], APPLICATION_JSON)
            assertEquals(it.path, "$GAX_URL$PERSONALITIES_PATH")
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
            assertEquals(it.method, Method.GET)
            assertEquals(it.headers[CONTENT_TYPE], APPLICATION_JSON)
            assertEquals(it.path, "$GAX_URL$AUDIO_MESSAGES_PATH")
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
            assertEquals(it.method, Method.POST)
            assertEquals(it.body(), """{"name":"$name","description":"$description","type":"ANNOUNCEMENT","privateResource":false}""")
            assertEquals(it.headers[CONTENT_TYPE], APPLICATION_JSON)
            assertEquals(it.path, "$GAX_URL$AUDIO_RESOURCES_PATH")
        }

        val messageUrl = createMessage("name", "description", GAX_URL)
        assertEquals(messageUrl, MESSAGE_URL)
    }

    @Test
    fun `createMessage should fail if message url is missing`() {
        mockHttpClient(CREATE_MESSAGE_SUCCESS_CODE)
        assertThrows(AudioServicesException::class.java) {
            createMessage("name", "description", GAX_URL)
        }
    }

    @Test
    fun `createMessage should properly handle error`() {
        mockHttpClient(statusCode = 666, headers = mapOf(LOCATION to listOf(MESSAGE_URL)))
        assertThrows(AudioServicesException::class.java) {
            createMessage("name", "description", GAX_URL)
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

private fun Request.body() = ByteArrayOutputStream().also {
    bodyCallback?.invoke(this, it, 0)
}.toString()
