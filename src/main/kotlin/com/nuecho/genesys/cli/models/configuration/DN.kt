package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNAccessNumber
import com.genesyslab.platform.configuration.protocol.types.CfgDNRegisterFlag
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgRouteType
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.UNCHANGED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.DNGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName

/**
 * @See https://docs.genesys.com/Documentation/PSDK/8.5.x/ConfigLayerRef/CfgDN
 */
data class DN(
    val tenant: TenantReference,
    val number: String,
    val switch: SwitchReference,
    val type: String,

    val registerAll: String? = CfgDNRegisterFlag.CFGDRTrue.toShortName(),
    val switchSpecificType: Int? = 1,

    val association: String? = null,

    val routing: Routing? = Routing(),

    val dnLoginID: String? = null,
    val group: DNGroupReference? = null,
    val trunks: Int? = 0,
    val override: String? = null,

    val state: String? = null,

    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,

    val name: String? = null,

    val useOverride: Boolean? = CfgFlag.CFGTrue.asBoolean(),
    val accessNumbers: List<DNAccessNumber>? = emptyList(),

    val site: FolderReference? = null,
    val contract: ObjectiveTableReference? = null

) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = DNReference(number, switch, type, name, tenant)

    constructor(dn: CfgDN) : this(
        tenant = TenantReference(dn.tenant.name),
        number = dn.number,
        switch = dn.switch.getReference(),
        type = dn.type!!.toShortName(),

        registerAll = dn.registerAll?.toShortName(),
        switchSpecificType = dn.switchSpecificType,

        association = dn.association,

        routing = dn.routeType?.let {
            Routing(
                type = dn.routeType.toShortName(),
                destinationDNs = dn.destDNs.map { it.getReference() }
            )
        },

        dnLoginID = dn.dnLoginID,
        group = dn.group?.getReference(),
        trunks = dn.trunks,
        override = dn.override,

        state = dn.state?.toShortName(),
        userProperties = dn.userProperties?.asCategorizedProperties(),

        name = dn.name,

        useOverride = dn.useOverride?.asBoolean(),

        accessNumbers = dn.accessNumbers?.map { DNAccessNumber(it) },
        site = dn.site?.getReference(),
        contract = dn.contract?.getReference()
    )

    override fun updateCfgObject(service: IConfService): ConfigurationObjectUpdateResult {
        service.retrieveObject(reference)?.let {
            return ConfigurationObjectUpdateResult(UNCHANGED, it)
        }

        CfgDN(service).let {
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("number", number, it)

            // Switch
            setProperty("switchDBID", service.getObjectDbid(switch), it)
            setProperty("registerAll", ConfigurationObjects.toCfgDNRegisterFlag(registerAll), it)
            setProperty("switchSpecificType", switchSpecificType, it)

            setProperty("type", ConfigurationObjects.toCfgDNType(type), it)
            setProperty("association", association, it)

            // Routing
            setProperty("routeType", ConfigurationObjects.toCfgRouteType(routing?.type), it)
            setProperty("destDNDBIDs", routing?.destinationDNs?.mapNotNull { service.getObjectDbid(it) }, it)

            setProperty("DNLoginID", dnLoginID, it)

            setProperty("groupDBID", service.getObjectDbid(group), it)
            setProperty("trunks", trunks, it)
            setProperty("override", override, it)

            setProperty("state", ConfigurationObjects.toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)

            setProperty("name", name, it)

            setProperty("useOverride", ConfigurationObjects.toCfgFlag(useOverride), it)
            setProperty("accessNumbers", toCfgDNAccessNumberList(accessNumbers, it), it)

            setProperty("siteDBID", service.getObjectDbid(site), it)

            setProperty("contractDBID", service.getObjectDbid(contract), it)

            return ConfigurationObjectUpdateResult(CREATED, it)
        }
    }

    override fun afterPropertiesSet() {
        switch.tenant = tenant
        routing?.destinationDNs?.forEach { it.tenant = tenant }
        group?.tenant = tenant
        accessNumbers?.forEach { it.updateTenantReferences(tenant) }
        contract?.tenant = tenant
    }
}

fun toCfgDNAccessNumberList(accessNumbers: List<DNAccessNumber>?, dn: CfgDN) =
    if (accessNumbers == null) null
    else {
        val service = dn.configurationService
        accessNumbers.map {
            val cfgDNAccessNumber = CfgDNAccessNumber(service, dn)

            cfgDNAccessNumber.setProperty("number", it.number)
            cfgDNAccessNumber.setProperty("switchDBID", service.getObjectDbid(it.switch))

            cfgDNAccessNumber
        }
    }

data class Routing(
    val type: String = CfgRouteType.CFGDirect.toShortName(),
    val destinationDNs: List<DNReference> = emptyList()
)
