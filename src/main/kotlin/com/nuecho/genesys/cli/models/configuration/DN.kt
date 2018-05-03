package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNAccessNumber
import com.genesyslab.platform.configuration.protocol.types.CfgDNRegisterFlag
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgRouteType
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.asMap
import com.nuecho.genesys.cli.getPrimaryKey
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.UNCHANGED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.services.getDNGroupDbid
import com.nuecho.genesys.cli.services.getFolderDbid
import com.nuecho.genesys.cli.services.getObjectiveTableDbid
import com.nuecho.genesys.cli.services.getSwitchDbid
import com.nuecho.genesys.cli.services.retrieveDN
import com.nuecho.genesys.cli.services.retrieveDNs
import com.nuecho.genesys.cli.toShortName

/**
 * @See https://docs.genesys.com/Documentation/PSDK/8.5.x/ConfigLayerRef/CfgDN
 */
data class DN(
    val number: String, // XXX: number could be tied to the Switch object
    val switch: DNSwitch,
    val type: String? = CfgDNType.CFGNoDN.toShortName(),

    val association: String? = null,

    val routing: Routing? = Routing(),

    val dnLoginID: String? = null,
    val group: String? = null,
    val trunks: Int? = 0,
    val override: String? = null,

    val state: String? = null,

    @JsonSerialize(using = KeyValueCollectionSerializer::class)
    @JsonDeserialize(using = KeyValueCollectionDeserializer::class)
    override val userProperties: Map<String, Any>? = null,

    val name: String? = null,

    val useOverride: Boolean? = CfgFlag.CFGTrue.asBoolean(),
    val accessNumbers: List<DNAccessNumber>? = emptyList(),
    val site: String? = null,
    val contract: String? = null

) : ConfigurationObject {
    override val primaryKey: String
        @JsonIgnore
        get() = number

    constructor(dn: CfgDN) : this(
        number = dn.number,
        switch = DNSwitch(dn.switch.name, dn.registerAll.toShortName(), dn.switchSpecificType),
        type = dn.type.toShortName(),

        association = dn.association,

        routing = dn.routeType?.let {
            Routing(
                type = dn.routeType.toShortName(),
                destinationDNs = dn.destDNs.map { it.number }.toSortedSet()
            )
        },

        dnLoginID = dn.dnLoginID,
        group = dn.group.getPrimaryKey(),
        trunks = dn.trunks,
        override = dn.override,

        state = dn.state?.toShortName(),
        userProperties = dn.userProperties?.asMap(),

        name = dn.name,

        useOverride = dn.useOverride.asBoolean(),

        accessNumbers = dn.accessNumbers.map { DNAccessNumber(it) },
        site = dn.site.getPrimaryKey(),
        contract = dn.contract.getPrimaryKey()
    )

    override fun updateCfgObject(service: IConfService): ConfigurationObjectUpdateResult {
        service.retrieveDN(number)?.let {
            return ConfigurationObjectUpdateResult(UNCHANGED, it)
        }

        CfgDN(service).let {
            setProperty("number", number, it)

            // Switch
            setProperty("switchDBID", service.getSwitchDbid(switch.name), it)
            setProperty("registerAll", ConfigurationObjects.toCfgDNRegisterFlag(switch.registerAll), it)
            setProperty("switchSpecificType", switch.switchSpecificType, it)

            setProperty("type", ConfigurationObjects.toCfgDNType(type), it)
            setProperty("association", association, it)

            // Routing
            setProperty("routeType", ConfigurationObjects.toCfgRouteType(routing?.type), it)
            setProperty("destDNDBIDs", service.retrieveDNs(routing?.destinationDNs), it)

            setProperty("DNLoginID", dnLoginID, it)

            setProperty("groupDBID", service.getDNGroupDbid(group), it)
            setProperty("trunks", trunks, it)
            setProperty("override", override, it)

            setProperty("state", ConfigurationObjects.toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)

            setProperty("name", name, it)

            setProperty("useOverride", ConfigurationObjects.toCfgFlag(useOverride), it)
            setProperty("accessNumbers", toCfgDNAccessNumberList(accessNumbers, it), it)

            setProperty("siteDBID", service.getFolderDbid(site), it)

            setProperty("contractDBID", service.getObjectiveTableDbid(contract), it)

            return ConfigurationObjectUpdateResult(CREATED, it)
        }
    }
}

fun toCfgDNAccessNumberList(accessNumbers: List<DNAccessNumber>?, dn: CfgDN) =
    if (accessNumbers == null) null
    else {
        val service = dn.configurationService
        accessNumbers.map {
            val cfgDNAccessNumber = CfgDNAccessNumber(service, dn)

            cfgDNAccessNumber.setProperty("number", it.number)
            cfgDNAccessNumber.setProperty("switchDBID", service.getSwitchDbid(it.switch))

            cfgDNAccessNumber
        }
    }

data class DNSwitch(
    val name: String,
    val registerAll: String? = CfgDNRegisterFlag.CFGDRTrue.toShortName(),
    val switchSpecificType: Int? = 1
)

data class Routing(
    val type: String = CfgRouteType.CFGDirect.toShortName(), val destinationDNs: Set<String> = emptySet()
)
