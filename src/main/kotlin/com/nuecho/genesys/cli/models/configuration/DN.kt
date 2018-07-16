package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNAccessNumber
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgDNRegisterFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgDNType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgRouteType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.DNGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

/**
 * @See https://docs.genesys.com/Documentation/PSDK/8.5.x/ConfigLayerRef/CfgDN
 */
data class DN(
    val tenant: TenantReference,
    val number: String,
    val switch: SwitchReference,
    val type: String,

    val registerAll: String? = null,
    val switchSpecificType: Int? = null,

    val association: String? = null,

    val routeType: String? = null,
    val destinationDNs: List<DNReference>? = null,

    val dnLoginID: String? = null,
    val group: DNGroupReference? = null,
    val trunks: Int? = null,
    val override: String? = null,

    val state: String? = null,

    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null,

    val name: String? = null,

    val useOverride: Boolean? = null,
    val accessNumbers: List<DNAccessNumber>? = null,

    val site: FolderReference? = null,
    val contract: ObjectiveTableReference? = null

) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = DNReference(number, switch, type, name, tenant)

    constructor(dn: CfgDN) : this(
        tenant = dn.tenant.getReference(),
        number = dn.number,
        switch = dn.switch.getReference(),
        type = dn.type!!.toShortName(),

        registerAll = dn.registerAll?.toShortName(),
        switchSpecificType = dn.switchSpecificType,

        association = dn.association,

        routeType = dn.routeType.toShortName(),
        destinationDNs = dn.destDNs?.map { it.getReference() },

        dnLoginID = dn.dnLoginID,
        group = dn.group?.getReference(),
        trunks = dn.trunks,
        override = dn.override,

        state = dn.state?.toShortName(),
        userProperties = dn.userProperties?.asCategorizedProperties(),
        folder = dn.getFolderReference(),

        name = dn.name,

        useOverride = dn.useOverride?.asBoolean(),

        accessNumbers = dn.accessNumbers?.map { DNAccessNumber(it) },
        site = dn.site?.getReference(),
        contract = dn.contract?.getReference()
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgDN(service).also { applyDefaultValues() })

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgDN).also {

            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("number", number, it)

            // Switch
            setProperty("switchDBID", service.getObjectDbid(switch), it)
            setProperty("registerAll", toCfgDNRegisterFlag(registerAll), it)
            setProperty("switchSpecificType", switchSpecificType, it)

            setProperty("type", toCfgDNType(type), it)
            setProperty("association", association, it)

            // Routing
            setProperty(ROUTE_TYPE, toCfgRouteType(routeType), it)
            setProperty("destDNDBIDs", destinationDNs?.mapNotNull { service.getObjectDbid(it) }, it)

            setProperty("DNLoginID", dnLoginID, it)

            setProperty("groupDBID", service.getObjectDbid(group), it)
            setProperty("trunks", trunks, it)
            setProperty("override", override, it)

            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)

            setProperty("name", name, it)

            setProperty("useOverride", toCfgFlag(useOverride), it)
            setProperty("accessNumbers", toCfgDNAccessNumberList(accessNumbers, it), it)

            setProperty("siteDBID", service.getObjectDbid(site), it)

            setProperty("contractDBID", service.getObjectDbid(contract), it)

            setFolder(folder, it)
        }

    override fun checkMandatoryProperties(): Set<String> =
        if (routeType == null) setOf(ROUTE_TYPE) else emptySet()

    override fun applyDefaultValues() {
        // registerAll = CfgDNRegisterFlag.CFGDRTrue.toShortName()
        // switchSpecificType  = 1
        // routeType = CfgRouteType.CFGDirect.toShortName()
        // destinationDNs = emptyList()
        // trunks  = 0
        // useOverride = CfgFlag.CFGTrue.asBoolean(),
        // accessNumbers = emptyList(),
    }

    override fun afterPropertiesSet() {
        switch.tenant = tenant
        destinationDNs?.forEach { it.tenant = tenant }
        group?.tenant = tenant
        accessNumbers?.forEach { it.updateTenantReferences(tenant) }
        contract?.tenant = tenant
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(tenant)
            .add(switch)
            .add(destinationDNs)
            .add(group)
            .add(accessNumbers?.map { it.switch })
            .add(site)
            .add(contract)
            .add(folder)
            .toSet()
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
