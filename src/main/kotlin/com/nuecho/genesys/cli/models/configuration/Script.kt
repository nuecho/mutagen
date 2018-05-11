package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.configuration.protocol.types.CfgScriptType
import com.nuecho.genesys.cli.Logging.warn
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.UNCHANGED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgScriptType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.services.retrieveScript
import com.nuecho.genesys.cli.toShortName

data class Script(
    val name: String,
    val type: String = CfgScriptType.CFGNoScript.toShortName(),
    val index: Int = 0,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null

) : ConfigurationObject {
    override val primaryKey: String
        @JsonIgnore
        get() = name

    constructor(script: CfgScript) : this(
        name = script.name,
        type = script.type.toShortName(),
        index = script.index,
        state = script.state?.toShortName(),
        userProperties = script.userProperties?.asCategorizedProperties()
    ) {
        script.resources?.let { warn { "Unsupported ResourceObject collection. Ignoring." } }
    }

    override fun updateCfgObject(service: IConfService): ConfigurationObjectUpdateResult {
        service.retrieveScript(name)?.let {
            return ConfigurationObjectUpdateResult(UNCHANGED, it)
        }

        CfgScript(service).let {
            setProperty("name", name, it)
            setProperty("type", toCfgScriptType(type), it)
            setProperty("index", index, it)

            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            return ConfigurationObjectUpdateResult(CREATED, it)
        }
    }
}
