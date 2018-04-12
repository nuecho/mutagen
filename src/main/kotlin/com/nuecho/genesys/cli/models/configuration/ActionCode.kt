package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.applicationblocks.com.objects.CfgSubcode
import com.nuecho.genesys.cli.asMap
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.UNCHANGED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgActionCodeType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.services.retrieveActionCode
import com.nuecho.genesys.cli.toShortName

data class ActionCode(
    val name: String,
    val type: String? = null,
    val code: String? = null,
    val subcodes: Map<String, String>? = null,
    val state: String? = null,
    @JsonSerialize(using = KeyValueCollectionSerializer::class)
    @JsonDeserialize(using = KeyValueCollectionDeserializer::class)
    val userProperties: Map<String, Any>? = null
) : ConfigurationObject {
    override val primaryKey: String
        @JsonIgnore
        get() = name

    constructor(actionCode: CfgActionCode) : this(
        name = actionCode.name,
        type = actionCode.type?.toShortName(),
        code = actionCode.code,
        subcodes = actionCode.subcodes?.map { it.name to it.code }?.toMap(),
        state = actionCode.state?.toShortName(),
        userProperties = actionCode.userProperties?.asMap()
    )

    override fun updateCfgObject(service: IConfService): ConfigurationObjectUpdateResult {
        service.retrieveActionCode(name)?.let {
            return ConfigurationObjectUpdateResult(UNCHANGED, it)
        }

        CfgActionCode(service).let {
            setProperty("name", name, it)
            setProperty("type", toCfgActionCodeType(type), it)
            setProperty("code", code, it)
            setProperty("subcodes", toCfgSubcodeList(subcodes, it), it)
            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            return ConfigurationObjectUpdateResult(CREATED, it)
        }
    }
}

private fun toCfgSubcodeList(subcodes: Map<String, String>?, actionCode: CfgActionCode) =
    subcodes?.map { (name, code) ->
        CfgSubcode(actionCode.configurationService, actionCode).apply {
            this.name = name
            this.code = code
        }
    }?.toList()