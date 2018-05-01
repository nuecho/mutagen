package com.nuecho.genesys.cli.commands.audio.import

import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.nuecho.genesys.cli.CliOutputCaptureWrapper
import com.nuecho.genesys.cli.TestResources.getTestResource
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.APPLICATION_JSON
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.AUDIO_RESOURCES_PATH
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.CONTENT_TYPE
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.CREATE_MESSAGE_SUCCESS_CODE
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.LOCATION
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.LOGIN_SUCCESS_CODE
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.UPLOAD_AUDIO_SUCCESS_CODE
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.UPLOAD_PATH
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.createMessage
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.importAudios
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.login
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.readAudioData
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.uploadAudio
import com.nuecho.genesys.cli.preferences.environment.Environment
import io.kotlintest.matchers.endWith
import io.kotlintest.matchers.include
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.matchers.startWith
import io.kotlintest.specs.StringSpec
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL

private const val USAGE_PREFIX = "Usage: import [-?]"
private const val GAX_URL = "http://genesys.com"
private const val MESSAGE_URL = "$GAX_URL/messages/1234"

class AudioImportCommandTest : StringSpec() {
    init {
        val audioCsv = getTestResource("commands/audio/audios.csv")
        val audioFile = File(getTestResource("commands/audio/audio.wav").toURI())

        // To disable the automatic redirection
        FuelManager.instance.removeAllResponseInterceptors()

        "executing Import with -h argument should print usage" {
            val output = CliOutputCaptureWrapper.execute("audio", "import", "-h")
            output should startWith(USAGE_PREFIX)
        }

        "readAudioData should properly deserialize audio data" {
            val audioData = readAudioData(audioCsv.openStream())
            audioData.next() shouldBe mapOf(
                "name" to "foo",
                "description" to "fooscription",
                "10014" to "foo.fr.wav",
                "10015" to "foo.en.wav"
            )

            audioData.next()
            audioData.next()
            audioData.hasNextValue() shouldBe false
        }

        "login should properly handle success" {
            val user = "user"
            val password = "password"

            mockHttpClient(LOGIN_SUCCESS_CODE) {
                it.method shouldBe Method.POST
                it.body() shouldBe """{"username":"$user","password":"$password","isPasswordEncrypted":false}"""
                it.headers[CONTENT_TYPE] shouldBe APPLICATION_JSON
                it.path shouldBe "$GAX_URL${AudioImport.LOGIN_PATH}"
            }

            login(user, password, false, GAX_URL)
        }

        "login should properly handle error" {
            mockHttpClient(666)
            shouldThrow<AudioImportException> {
                login("user", "password", false, GAX_URL)
            }
        }

        "createMessage should return the message url" {
            val name = "name"
            val description = "description"

            mockHttpClient(
                CREATE_MESSAGE_SUCCESS_CODE,
                mapOf(LOCATION to listOf(MESSAGE_URL))
            ) {
                it.method shouldBe Method.POST
                it.body() shouldBe """{"name":"$name","description":"$description","type":"ANNOUNCEMENT","privateResource":false}"""
                it.headers[CONTENT_TYPE] shouldBe APPLICATION_JSON
                it.path shouldBe "$GAX_URL$AUDIO_RESOURCES_PATH"
            }

            val messageUrl = createMessage("name", "description", GAX_URL)
            messageUrl shouldBe MESSAGE_URL
        }

        "createMessage should fail if message url is missing" {
            mockHttpClient(CREATE_MESSAGE_SUCCESS_CODE)
            shouldThrow<AudioImportException> {
                createMessage("name", "description", GAX_URL)
            }
        }

        "createMessage should properly handle error" {
            mockHttpClient(666, mapOf(LOCATION to listOf(MESSAGE_URL)))
            shouldThrow<AudioImportException> {
                createMessage("name", "description", GAX_URL)
            }
        }

        "uploadAudio should properly build the Request" {
            val personality = "12345"
            val callbackSequenceNumber = 1

            mockHttpClient(UPLOAD_AUDIO_SUCCESS_CODE) {
                it.method shouldBe Method.POST
                it.body() should include(personality)
                it.path should startWith("$MESSAGE_URL$UPLOAD_PATH")
                it.path should endWith(callbackSequenceNumber.toString())
            }

            uploadAudio(MESSAGE_URL, audioFile, personality, callbackSequenceNumber)
        }

        "uploadAudio should properly handle error" {
            val personality = "12345"
            val callbackSequenceNumber = 1

            mockHttpClient(666)

            shouldThrow<AudioImportException> {
                uploadAudio(MESSAGE_URL, audioFile, personality, callbackSequenceNumber)
            }
        }

        "importAudios should fail when name column is missing" {
            shouldThrow<AudioImportException> {
                importAudios(
                    Environment(host = "host", user = "user", rawPassword = "password"),
                    audioCsv.openStream(),
                    File("audioDirectory")
                )
            }
        }
    }
}

private fun mockHttpClient(
    statusCode: Int,
    headers: Map<String, List<String>> = emptyMap(),
    validateRequest: (request: Request) -> Unit = {}
) {
    val client = object : Client {
        override fun executeRequest(request: Request): Response {
            validateRequest(request)
            return Response(
                url = URL(GAX_URL),
                statusCode = statusCode,
                headers = headers
            )
        }
    }

    FuelManager.instance.client = client
}

private fun Request.body() = ByteArrayOutputStream().also {
    bodyCallback?.invoke(this, it, 0)
}.toString()
