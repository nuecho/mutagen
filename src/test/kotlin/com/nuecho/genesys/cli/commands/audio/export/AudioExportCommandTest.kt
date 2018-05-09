package com.nuecho.genesys.cli.commands.audio.export

import com.fasterxml.jackson.core.type.TypeReference
import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.google.common.base.Charsets
import com.nuecho.genesys.cli.CliOutputCaptureWrapper
import com.nuecho.genesys.cli.TestResources.getTestResource
import com.nuecho.genesys.cli.commands.audio.AudioServices.APPLICATION_JSON
import com.nuecho.genesys.cli.commands.audio.AudioServices.CONTENT_TYPE
import com.nuecho.genesys.cli.commands.audio.AudioServices.DESCRIPTION
import com.nuecho.genesys.cli.commands.audio.AudioServices.MESSAGE_AR_ID
import com.nuecho.genesys.cli.commands.audio.AudioServices.NAME
import com.nuecho.genesys.cli.commands.audio.AudioServices.TENANT_ID
import com.nuecho.genesys.cli.commands.audio.AudioServicesException
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.AUDIO_MESSAGES_PATH
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.DOWNLOAD_AUDIO_SUCCESS_CODE
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.PERSONALITIES_PATH
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.getPersonalities
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.buildSchema
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.getAudioData
import com.nuecho.genesys.cli.commands.audio.export.AudioExport.writeAudioData
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
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

class AudioExportCommandTest : StringSpec() {
    init {
        val messagesData = File(getTestResource("commands/audio/messagesData.csv").toURI())
        val personalitiesData = File(getTestResource("commands/audio/personalitiesData.csv").toURI())
        val audioCsv = File(getTestResource("commands/audio/audioOutput.csv").toURI())
        val expectedAudioCsv = File(getTestResource("commands/audio/audioOutput.csv").toURI())

        // To disable the automatic redirection
        FuelManager.instance.removeAllResponseInterceptors()

        "executing Export with -h argument should print usage" {
            val output = CliOutputCaptureWrapper.execute("audio", "export", "-h")
            output should startWith(USAGE_PREFIX)
        }

        "getPersonalities should fail when the response status code is wrong" {
            mockHttpClient(
                INTERNAL_SERVER_ERROR,
                ByteArrayInputStream("".toByteArray())
            )
            shouldThrow<AudioServicesException> {
                getPersonalities(GAX_URL)
            }
        }

        "writeAudioData should fail when the response status code is wrong" {

            mockHttpClient(DOWNLOAD_AUDIO_SUCCESS_CODE, personalitiesData.inputStream())
            val schemaBuilder = buildSchema(getPersonalities(GAX_URL))

            mockHttpClient(
                INTERNAL_SERVER_ERROR,
                messagesData.inputStream()
            )
            shouldThrow<AudioServicesException> {
                writeAudioData(
                    getAudioData(GAX_URL),
                    schemaBuilder,
                    audioCsv.outputStream()
                )
            }
        }

        "getPersonalities should properly build the Request" {

            mockHttpClient(
                DOWNLOAD_AUDIO_SUCCESS_CODE, personalitiesData.inputStream()
            ) {
                it.method shouldBe Method.GET
                it.headers[CONTENT_TYPE] shouldBe APPLICATION_JSON
                it.path shouldBe "$GAX_URL$PERSONALITIES_PATH"
            }

            getPersonalities(GAX_URL)
        }

        "writeAudioData should properly build the Request" {

            mockHttpClient(DOWNLOAD_AUDIO_SUCCESS_CODE, personalitiesData.inputStream())
            val schemaBuilder = buildSchema(getPersonalities(GAX_URL))

            mockHttpClient(
                DOWNLOAD_AUDIO_SUCCESS_CODE,
                messagesData.inputStream()
            ) {
                it.method shouldBe Method.GET
                it.headers[CONTENT_TYPE] shouldBe APPLICATION_JSON
                it.path shouldBe "$GAX_URL$AUDIO_MESSAGES_PATH"
            }

            writeAudioData(
                getAudioData(GAX_URL),
                schemaBuilder,
                audioCsv.outputStream()
            )
        }

        "buildSchema should properly build csv schema builder from map" {
            mockHttpClient(DOWNLOAD_AUDIO_SUCCESS_CODE, personalitiesData.inputStream())

            val schema = buildSchema(getPersonalities(GAX_URL))

            schema.hasColumn(NAME) shouldBe true
            schema.hasColumn(DESCRIPTION) shouldBe true
            schema.hasColumn(MESSAGE_AR_ID) shouldBe true
            schema.hasColumn(TENANT_ID) shouldBe true
            schema.hasColumn("10002") shouldBe true
            schema.hasColumn("10004") shouldBe true
            schema.hasColumn("10015") shouldBe true
            schema.hasColumn("10003") shouldBe true
            schema.hasColumn("10005") shouldBe true

            schema.size() shouldBe 9
        }

        "writeCsvFile should properly write in csv file" {
            mockHttpClient(DOWNLOAD_AUDIO_SUCCESS_CODE, personalitiesData.inputStream())

            val personalities = getPersonalities(GAX_URL)
            val responseItems: List<ResponseItem> = defaultJsonObjectMapper().readValue(messagesData.inputStream(), object : TypeReference<List<ResponseItem>>() {})

            writeAudioData( responseItems, buildSchema(personalities), audioCsv.outputStream())

            audioCsv.readText(Charsets.UTF_8) shouldBe expectedAudioCsv.readText(Charsets.UTF_8)
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
