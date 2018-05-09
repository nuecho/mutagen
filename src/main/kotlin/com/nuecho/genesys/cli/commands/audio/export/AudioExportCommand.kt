package com.nuecho.genesys.cli.commands.audio.export

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.commands.audio.Audio
import com.nuecho.genesys.cli.commands.audio.AudioServices.APPLICATION_JSON
import com.nuecho.genesys.cli.commands.audio.AudioServices.AUDIO_RESOURCES_PATH
import com.nuecho.genesys.cli.commands.audio.AudioServices.CONTENT_TYPE
import com.nuecho.genesys.cli.commands.audio.AudioServices.DESCRIPTION
import com.nuecho.genesys.cli.commands.audio.AudioServices.GAX_BASE_PATH
import com.nuecho.genesys.cli.commands.audio.AudioServices.HTTP
import com.nuecho.genesys.cli.commands.audio.AudioServices.MESSAGE_AR_ID
import com.nuecho.genesys.cli.commands.audio.AudioServices.NAME
import com.nuecho.genesys.cli.commands.audio.AudioServices.TENANT_ID
import com.nuecho.genesys.cli.commands.audio.AudioServices.checkStatusCode
import com.nuecho.genesys.cli.commands.audio.AudioServices.login
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import picocli.CommandLine
import java.io.File
import java.io.OutputStream
import java.net.CookieHandler
import java.net.CookieManager

@CommandLine.Command(
    name = "export",
    description = ["Export audio files from ARM"]
)
class AudioExportCommand : GenesysCliCommand() {
    @CommandLine.ParentCommand
    private var audio: Audio? = null

    @CommandLine.Parameters(
        arity = "1",
        index = "0",
        paramLabel = "outputFile",
        description = ["Output audio description file."]
    )
    private var outputFile: File? = null

    override fun getGenesysCli(): GenesysCli = audio!!.getGenesysCli()

    override fun execute() {

        AudioExport.exportAudios(
            getGenesysCli().loadEnvironment(),
            outputFile!!.outputStream()
        )
    }
}

object AudioExport {
    // URL Components
    const val PERSONALITIES_PATH = "$GAX_BASE_PATH/arm/personalities/"
    const val AUDIO_MESSAGES_PATH = "$AUDIO_RESOURCES_PATH/?tenantList=1"

    // Status Code
    const val DOWNLOAD_AUDIO_SUCCESS_CODE = 200

    //exportAudios
    fun exportAudios(environment: Environment, audioData: OutputStream) {
        FuelManager.instance.removeAllResponseInterceptors()
        CookieHandler.setDefault(CookieManager())

        val gaxUrl = "$HTTP${environment.host}:${environment.port}"

        info { "Logging in to GAX as '${environment.user}'." }
        login(environment.user, environment.password!!, true, gaxUrl)

        val csvSchemaBuilder = buildSchema(getPersonalities(gaxUrl))
        val responseItems = getAudioData(gaxUrl)
        writeAudioData(responseItems, csvSchemaBuilder, audioData)
    }

    internal fun getAudioData(gaxUrl: String): List<ResponseItem> {
        "$gaxUrl$AUDIO_MESSAGES_PATH"
            .httpGet()
            .header(CONTENT_TYPE to APPLICATION_JSON)
            .responseString()
            .let { (request, response, result) ->
                checkStatusCode(DOWNLOAD_AUDIO_SUCCESS_CODE, "Failed to download audio.", request, response, result)

                return defaultJsonObjectMapper().readValue(
                    response.data.inputStream(),
                    object : TypeReference<List<ResponseItem>>() {}
                )
            }
    }

    internal fun writeAudioData(
        responseItems: List<ResponseItem>,
        csvSchemaBuilder: CsvSchema.Builder,
        outputStream: OutputStream
    ) {
        val mapper = CsvMapper()
        mapper.enable(JsonGenerator.Feature.IGNORE_UNKNOWN)

        val maps = responseItems.map { item ->
            val actualMap: MutableMap<String, String> = mutableMapOf(
                NAME to item.name,
                DESCRIPTION to item.description,
                MESSAGE_AR_ID to item.arId.toString(),
                TENANT_ID to item.tenantId.toString()
            )

            for (audioResource in item.ownResourceFiles) {
                actualMap[audioResource.personality.id.toString()] = audioResource.originalFilename
            }
            actualMap
        }.toList()

        CsvMapper()
            .writerFor(Map::class.java)
            .with(csvSchemaBuilder
                .build()
                .sortedBy(NAME, DESCRIPTION, MESSAGE_AR_ID, TENANT_ID)
                .withHeader()
            )
            .writeValues(outputStream).writeAll(maps)
    }

    internal fun getPersonalities(gaxUrl: String): List<Personality> {
        "$gaxUrl$PERSONALITIES_PATH"
            .httpGet()
            .header(CONTENT_TYPE to APPLICATION_JSON)
            .responseString()
            .let { (request, response, result) ->
                checkStatusCode(
                    DOWNLOAD_AUDIO_SUCCESS_CODE,
                    "Failed to download personalities.",
                    request,
                    response,
                    result)
                return defaultJsonObjectMapper().readValue(
                    response.data.inputStream(),
                    object : TypeReference<List<Personality>>() {}
                )
            }
    }

    internal fun buildSchema(personalities: List<Personality> ): CsvSchema.Builder {

        val csvSchemaBuilder = CsvSchema.Builder()

        csvSchemaBuilder.addColumn(NAME)
        csvSchemaBuilder.addColumn(DESCRIPTION)
        csvSchemaBuilder.addColumn(TENANT_ID)
        csvSchemaBuilder.addColumn(MESSAGE_AR_ID)

        for (personality in personalities) {
            csvSchemaBuilder.addColumn(personality.id.toString())
        }

        return csvSchemaBuilder
    }
}

internal data class ResponseItem(
    val name: String,
    val description: String,
    val id: Int,
    val arId: Int,
    val creatorTenantDbid: Int,
    val creatorTenantName: String,
    val tenantId: Int,
    val tenantName: String?,
    val type: String,
    val editable: Boolean,
    val copy: Boolean,
    val preinstalled: Boolean,
    val privateResource: Boolean,
    val deployedResourceFiles: ArrayList<String> ,
    val ownResourceFiles: ArrayList<OwnResourceFile>

)

internal data class OwnResourceFile(
    val id: Int,
    val arfId: Int,
    val originalFilename: String,
    val fileStatus: String,
    val is_copy: Boolean,
    val encoding: String,
    val originalFileSize: Int,
    val preinstalled: Boolean,
    val Timestamp: String,
    val personality: EmbeddedPersonality

)

internal data class EmbeddedPersonality(
    val id: Int,
    val name: String,
    val tenantDbId: Int,
    val logicalId: Int,
    val gender: String,
    val language: String,
    val description: String,
    val personalityId: Int,
    val privateResource: Boolean,
    val preinstalled: Boolean

)

internal data class Personality(
    val id: Int,
    val name: String,
    val tenantId: Int,
    val creatorTenantDbId: Int,
    val tenantName: String,
    val logicalId: Int,
    val gender: String,
    val language: String,
    val description: String,
    val personalityId: Int,
    val privateResource: Boolean,
    val preinstalled: Boolean,
    val editable: Boolean,
    val copy: Boolean
)
