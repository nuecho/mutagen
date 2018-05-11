package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.nuecho.genesys.cli.getPrimaryKey
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.UNCHANGED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgLinkType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.services.getApplicationDbid
import com.nuecho.genesys.cli.services.getPhysicalSwitchDbid
import com.nuecho.genesys.cli.services.retrieveSwitch
import com.nuecho.genesys.cli.toShortName

/**
 * Unused fields address and contactPersonDBID are not defined.
 */
data class Switch(
    val name: String,
    val physicalSwitch: String? = null,
    @get:JsonProperty("tServer") val tServer: String? = null,
    val linkType: String? = null,
    val switchAccessCodes: List<SwitchAccessCode>? = null,
    val dnRange: String? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null
) : ConfigurationObject {
    override val primaryKey: String
        @JsonIgnore
        get() = name

    constructor(switch: CfgSwitch) : this(
        name = switch.getPrimaryKey(),
        physicalSwitch = switch.physSwitch?.getPrimaryKey(),
        tServer = switch.tServer?.getPrimaryKey(),
        linkType = switch.linkType?.toShortName(),
        switchAccessCodes = switch.switchAccessCodes?.map { SwitchAccessCode(it) },
        dnRange = switch.dnRange,
        state = switch.state?.toShortName(),
        userProperties = switch.userProperties?.asCategorizedProperties()
    )

    override fun updateCfgObject(service: IConfService): ConfigurationObjectUpdateResult {
        service.retrieveSwitch(name)?.let {
            return ConfigurationObjectUpdateResult(UNCHANGED, it)
        }

        CfgSwitch(service).let { switch ->
            setProperty("name", name, switch)
            setProperty("physSwitchDBID", service.getPhysicalSwitchDbid(physicalSwitch), switch)
            setProperty("TServerDBID", service.getApplicationDbid(tServer), switch)
            setProperty("linkType", toCfgLinkType(linkType), switch)
            setProperty(
                "switchAccessCodes",
                switchAccessCodes?.map { accessCode -> accessCode.toCfgSwitchAccessCode(service, switch) },
                switch
            )
            setProperty("DNRange", dnRange, switch)
            setProperty("state", toCfgObjectState(state), switch)
            setProperty("userProperties", toKeyValueCollection(userProperties), switch)

            return ConfigurationObjectUpdateResult(CREATED, switch)
        }
    }
}
