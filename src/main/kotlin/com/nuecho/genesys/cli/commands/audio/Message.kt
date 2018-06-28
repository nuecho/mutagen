package com.nuecho.genesys.cli.commands.audio

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.nuecho.genesys.cli.commands.audio.MessageType.ANNOUNCEMENT
import java.util.TreeMap

@JsonDeserialize(builder = Message.Builder::class)
@JsonPropertyOrder("name", "type", "description", "messageArId", "tenantId")
data class Message(
    val name: String,
    val type: MessageType,
    val description: String = "",
    val messageArId: Int? = null,
    val tenantId: Int? = null,
    @get:JsonAnyGetter
    val audioByPersonality: Map<String, String> = sortedMapOf()
) {
    constructor(message: ArmMessage) :
            this(
                message.name,
                message.type,
                message.description,
                message.arId,
                message.tenantId,
                message.ownResourceFiles.map {
                    val personalityId = it.personality.personalityId
                    Pair(personalityId, "$personalityId/${message.name}.wav")
                }.toMap()
            )

    class Builder(
        val name: String,
        val type: MessageType = ANNOUNCEMENT,
        val description: String = "",
        val messageArId: Int? = null,
        val tenantId: Int? = null,
        val audioByPersonality: MutableMap<String, String> = TreeMap()
    ) {
        @JsonAnySetter
        fun addAudio(personality: String, audioFile: String): Builder {
            if (!audioFile.isBlank())
                audioByPersonality[personality] = audioFile
            return this
        }

        fun withAudios(vararg audios: Pair<String, String>): Builder {
            audioByPersonality.putAll(audios)
            return this
        }

        fun build() = Message(name, type, description, messageArId, tenantId, audioByPersonality.toMap())
    }
}
