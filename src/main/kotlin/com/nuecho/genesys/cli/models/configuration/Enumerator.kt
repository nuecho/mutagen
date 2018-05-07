package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.UNCHANGED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgEnumeratorType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.services.retrieveEnumerator
import com.nuecho.genesys.cli.toShortName

data class Enumerator(
    val name: String,
    val displayName: String? = null,
    val description: String? = null,
    val type: String? = null,
    val state: String? = null,
    @JsonSerialize(using = KeyValueCollectionSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null
) : ConfigurationObject {
    override val primaryKey: String
        @JsonIgnore
        get() = name

    constructor(enumerator: CfgEnumerator) : this(
        name = enumerator.name,
        displayName = enumerator.displayName,
        description = enumerator.description,
        type = enumerator.type?.toShortName(),
        state = enumerator.state?.toShortName(),
        userProperties = enumerator.userProperties?.asCategorizedProperties()
    )

    override fun updateCfgObject(service: IConfService): ConfigurationObjectUpdateResult {
        service.retrieveEnumerator(name)?.let {
            return ConfigurationObjectUpdateResult(UNCHANGED, it)
        }

        CfgEnumerator(service).let {
            setProperty("name", name, it)
            setProperty("displayName", displayName ?: name, it)
            setProperty("description", description, it)
            setProperty("type", toCfgEnumeratorType(type), it)
            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", ConfigurationObjects.toKeyValueCollection(userProperties), it)
            return ConfigurationObjectUpdateResult(CREATED, it)
        }
    }
}
