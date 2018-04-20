package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.nuecho.genesys.cli.asMap
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.UNCHANGED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgSwitchType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.services.retrievePhysicalSwitch
import com.nuecho.genesys.cli.toShortName

/**
 * Unused fields address and contactPersonDBID is not defined.
 */
data class PhysicalSwitch(
    val name: String,
    val type: String? = null,
    val state: String? = null,
    @JsonSerialize(using = KeyValueCollectionSerializer::class)
    @JsonDeserialize(using = KeyValueCollectionDeserializer::class)
    override val userProperties: Map<String, Any>? = null
) : ConfigurationObject {
    override val primaryKey: String
        @JsonIgnore
        get() = name

    constructor(physicalSwitch: CfgPhysicalSwitch) : this(
        name = physicalSwitch.name,
        type = physicalSwitch.type?.toShortName(),
        state = physicalSwitch.state?.toShortName(),
        userProperties = physicalSwitch.userProperties?.asMap()
    )

    override fun updateCfgObject(service: IConfService): ConfigurationObjectUpdateResult {
        service.retrievePhysicalSwitch(name)?.let {
            return ConfigurationObjectUpdateResult(UNCHANGED, it)
        }

        CfgPhysicalSwitch(service).let {
            setProperty("name", name, it)
            setProperty("type", toCfgSwitchType(type), it)
            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            return ConfigurationObjectUpdateResult(CREATED, it)
        }
    }
}
