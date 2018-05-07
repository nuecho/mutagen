package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.UNCHANGED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgLinkType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.ApplicationReference
import com.nuecho.genesys.cli.models.configuration.reference.PhysicalSwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName

/**
 * Unused fields address and contactPersonDBID are not defined.
 */
data class Switch(
    val tenant: TenantReference,
    val name: String,

    val physicalSwitch: PhysicalSwitchReference? = null,

    @get:JsonProperty("tServer")
    val tServer: ApplicationReference? = null,

    val linkType: String? = null,
    val switchAccessCodes: List<SwitchAccessCode>? = null,
    val dnRange: String? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null
) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = SwitchReference(name)

    constructor(switch: CfgSwitch) : this(
        tenant = TenantReference(switch.tenant.name),
        name = switch.name,
        physicalSwitch = switch.physSwitch?.getReference(),
        tServer = switch.tServer?.getReference(),
        linkType = switch.linkType?.toShortName(),
        switchAccessCodes = switch.switchAccessCodes?.map { SwitchAccessCode(it) },
        dnRange = switch.dnRange,
        state = switch.state?.toShortName(),
        userProperties = switch.userProperties?.asCategorizedProperties()
    )

    override fun updateCfgObject(service: IConfService): ConfigurationObjectUpdateResult {
        service.retrieveObject(reference)?.let {
            return ConfigurationObjectUpdateResult(UNCHANGED, it)
        }

        CfgSwitch(service).let { switch ->
            setProperty("tenantDBID", service.getObjectDbid(tenant), switch)
            setProperty("name", name, switch)
            setProperty("physSwitchDBID", service.getObjectDbid(physicalSwitch), switch)
            setProperty("TServerDBID", service.getObjectDbid(tServer), switch)
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
