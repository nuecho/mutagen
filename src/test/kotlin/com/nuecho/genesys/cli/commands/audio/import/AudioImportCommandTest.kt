package com.nuecho.genesys.cli.commands.audio.import

import com.fasterxml.jackson.core.type.TypeReference
import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.nuecho.genesys.cli.CliOutputCaptureWrapper
import com.nuecho.genesys.cli.TestResources.getTestResource
import com.nuecho.genesys.cli.commands.audio.AudioServicesException
import com.nuecho.genesys.cli.commands.audio.Message
import com.nuecho.genesys.cli.commands.audio.Personality
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.checkDuplicatedMessagesNames
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.checkMissingAudioFiles
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.checkMissingPersonalities
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.getPersonalitiesIdsMap
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.importAudios
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.readAudioData
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.findMissingAudioFiles
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.findExistingMessagesNames
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.getMissingPersonalities
import com.nuecho.genesys.cli.commands.audio.import.AudioImport.checkForInvalidElements

import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.preferences.environment.Environment
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.matchers.startWith
import io.kotlintest.specs.StringSpec
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL

private const val USAGE_PREFIX = "Usage: import [-?]"
private const val GAX_URL = "http://genesys.com"

class AudioImportCommandTest : StringSpec() {
    init {
        val audioCsv = getTestResource("commands/audio/audios.csv")

        val personalitiesData = File(getTestResource("commands/audio/personalities_data.json").toURI())
        val personalities: Set<Personality> = defaultJsonObjectMapper().readValue(personalitiesData.inputStream(), object : TypeReference<Set<Personality>>() {})
        val personalitiesMap = getPersonalitiesIdsMap(personalities)
        val newMessages = readAudioData(audioCsv.openStream()).readAll()

        // To disable the automatic redirection
        FuelManager.instance.removeAllResponseInterceptors()

        "executing Import with -h argument should print usage" {
            val output = CliOutputCaptureWrapper.execute("audio", "import", "-h")
            output should startWith(USAGE_PREFIX)
        }

        "importAudios should fail when name column is missing" {
            shouldThrow<AudioServicesException> {
                importAudios(
                    Environment(host = "host", user = "user", rawPassword = "password"),
                    audioCsv.openStream(),
                    File("audioDirectory")
                )
            }
        }

        "readAudioData should properly deserialize audio messagesData" {
            val audioData = readAudioData(audioCsv.openStream())
            audioData.next() shouldBe mapOf(
                "name" to "foo",
                "description" to "fooscription",
                "12" to "foo.fr.wav",
                "14" to "foo.en.wav",
                "10" to ""
            )

            audioData.next()
            audioData.next()
            audioData.hasNextValue() shouldBe false
        }

        "warnOfInvalidElements should return whether the list of invalid elements is empty or not" {
            shouldThrow<AudioImportException> {
                checkForInvalidElements(listOf("test"), "")
            }
        }

        "checkMissingPersonalities should throw AudioImportException when there is a missing perosnality" {
            shouldThrow<AudioImportException> {
                checkMissingPersonalities(newMessages, personalitiesMap)
            }
        }

        "checkDuplicatedMessagesNames should throw AudioImportException when there are duplicated messages" {
            shouldThrow<AudioImportException> {
                checkDuplicatedMessagesNames(newMessages, listOf(Message("foo", "", "", 0, 0, arrayListOf()))) shouldBe true
            }
        }

        "checkMissingAudioFiles should throw AudioImportException when there are files that don't exist" {
            shouldThrow<AudioImportException> {
                checkMissingAudioFiles(newMessages, File(audioCsv.toURI()).parentFile)
            }
        }

        "findMissingAudioFiles should only return files that don't exist" {
            val audioDirectoryPath = File(audioCsv.toURI()).parent

            findMissingAudioFiles(newMessages, audioDirectoryPath) shouldEqual listOf(
                File(audioDirectoryPath, "foo.fr.wav").absolutePath,
                File(audioDirectoryPath, "foo.en.wav").absolutePath,
                File(audioDirectoryPath, "bar.fr.wav").absolutePath,
                File(audioDirectoryPath, "baz.fr.wav").absolutePath,
                File(audioDirectoryPath, "baz.en.wav").absolutePath
            )
        }

        "findExistingMessagesNames should return file names that already exist, without case sensitivity" {
            val messagesData = File(getTestResource("commands/audio/messages_data.json").toURI())
            val existingMessages: List<Message> = defaultJsonObjectMapper().readValue(messagesData.inputStream(), object : TypeReference<List<Message>>() {})

            findExistingMessagesNames(newMessages, existingMessages) shouldBe listOf(
                "foo",
                "baz"
            )
        }

        "getPersonalitiesIdsMap should properly map the personalities' ids to their personalityIds" {
            getPersonalitiesIdsMap(personalities) shouldBe mapOf(
                "10" to "10002",
                "12" to "10004"
            )
        }

        "getMissingPersonalities should return personalities that don't exist" {
            getMissingPersonalities(newMessages, personalitiesMap) shouldBe listOf("14")
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
