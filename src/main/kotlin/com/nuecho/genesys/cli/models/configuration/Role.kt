package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgRole
import com.nuecho.genesys.cli.asMap
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.UNCHANGED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.dbidToPrimaryKey
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.services.retrieveRole
import com.nuecho.genesys.cli.toShortName
import java.util.SortedSet

class Role(
    val name: String,
    val description: String? = null,
    val state: String? = null,
    val members: SortedSet<String>? = null,
    @JsonSerialize(using = KeyValueCollectionSerializer::class)
    @JsonDeserialize(using = KeyValueCollectionDeserializer::class)
    val userProperties: Map<String, Any>? = null
) : ConfigurationObject {
    override val primaryKey: String
        @JsonIgnore
        get() = name

    constructor(role: CfgRole) : this(
        name = role.name,
        description = role.description,
        state = role.state?.toShortName(),
        userProperties = role.userProperties?.asMap(),
        members = role.members?.map {
            val type = it.objectType.toShortName()
            val key = dbidToPrimaryKey(it.objectDBID, it.objectType, role.configurationService)
            "$type/$key"
        }?.toSortedSet()
    )

    override fun updateCfgObject(service: IConfService): ConfigurationObjectUpdateResult {
        service.retrieveRole(name)?.let {
            return ConfigurationObjectUpdateResult(UNCHANGED, it)
        }

        // members are not exported
        CfgRole(service).let {
            setProperty("name", name, it)
            setProperty("description", description, it)
            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            return ConfigurationObjectUpdateResult(CREATED, it)
        }
    }
}
