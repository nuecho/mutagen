package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgSwitchType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.PhysicalSwitchReference
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName

/**
 * Unused fields address and contactPersonDBID is not defined.
 */
data class PhysicalSwitch(
    val name: String,
    val type: String? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null
) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = PhysicalSwitchReference(name)

    constructor(physicalSwitch: CfgPhysicalSwitch) : this(
        name = physicalSwitch.name,
        type = physicalSwitch.type?.toShortName(),
        state = physicalSwitch.state?.toShortName(),
        userProperties = physicalSwitch.userProperties?.asCategorizedProperties()
    )

    override fun updateCfgObject(service: IConfService) =
        (service.retrieveObject(reference) ?: CfgPhysicalSwitch(service)).also {
            setProperty("name", name, it)
            setProperty(TYPE, toCfgSwitchType(type), it)
            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
        }

    override fun checkMandatoryProperties(): Set<String> =
        if (type == null) setOf(TYPE) else emptySet()

    override fun getReferences(): Set<ConfigurationObjectReference<*>> = setOf()
}
