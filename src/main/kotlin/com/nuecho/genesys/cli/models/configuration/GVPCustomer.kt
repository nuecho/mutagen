package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPCustomer
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.GVPCustomerReference
import com.nuecho.genesys.cli.models.configuration.reference.GVPResellerReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.TimeZoneReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

/**
 * @See https://docs.genesys.com/Documentation/PSDK/8.5.x/ConfigLayerRef/CfgGVPCustomer
 */
data class GVPCustomer(
    val name: String,
    val tenant: TenantReference? = null,
    val reseller: GVPResellerReference? = null,
    val channel: String? = null,
    val displayName: String? = null,
    val notes: String? = null,
    @get:JsonProperty("isProvisioned")
    val isProvisioned: Boolean? = null,
    @get:JsonProperty("isAdminCustomer")
    val isAdminCustomer: Boolean? = null,
    val timeZone: TimeZoneReference? = null,

    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null

) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = GVPCustomerReference(name)

    constructor(gvpCustomer: CfgGVPCustomer) : this(
        name = gvpCustomer.name,
        tenant = gvpCustomer.tenant.getReference(),
        reseller = gvpCustomer.reseller.getReference(),

        channel = gvpCustomer.channel,
        displayName = gvpCustomer.displayName,
        notes = gvpCustomer.notes,
        isProvisioned = gvpCustomer.isProvisioned?.asBoolean(),
        isAdminCustomer = gvpCustomer.isAdminCustomer?.asBoolean(),

        timeZone = gvpCustomer.timeZone?.getReference(),

        state = gvpCustomer.state?.toShortName(),
        userProperties = gvpCustomer.userProperties?.asCategorizedProperties(),
        folder = gvpCustomer.getFolderReference()
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgGVPCustomer(service)).also {
            setProperty("name", name, it)
            setProperty("resellerDBID", service.getObjectDbid(reseller), it)
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setFolder(folder, it)
        }

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgGVPCustomer).also {
            setProperty(CHANNEL, channel, it)
            setProperty("displayName", displayName ?: name, it)
            setProperty("notes", notes, it)
            setProperty("isProvisioned", toCfgFlag(isProvisioned), it)
            setProperty("isAdminCustomer", toCfgFlag(isAdminCustomer), it)
            setProperty("timeZoneDBID", service.getObjectDbid(timeZone), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            setProperty("state", toCfgObjectState(state), it)
        }

    override fun cloneBare() = GVPCustomer(
        channel = channel,
        isAdminCustomer = isAdminCustomer,
        isProvisioned = isProvisioned,
        name = name,
        reseller = reseller,
        tenant = tenant
    )

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> {
        val missingMandatoryProperties = mutableSetOf<String>()
        channel ?: missingMandatoryProperties.add(CHANNEL)
        isAdminCustomer ?: missingMandatoryProperties.add(IS_ADMIN_CUSTOMER)
        isProvisioned ?: missingMandatoryProperties.add(IS_PROVISIONED)
        reseller ?: missingMandatoryProperties.add(RESELLER)
        tenant ?: missingMandatoryProperties.add(TENANT)

        return missingMandatoryProperties
    }

    override fun checkUnchangeableProperties(cfgObject: CfgObject) =
        checkUnchangeableProperties(this, cfgObject).also { unchangeableProperties ->
            (cfgObject as CfgGVPCustomer).also {
                reseller?.run { if (this != it.reseller?.getReference()) unchangeableProperties.add(RESELLER) }
                tenant?.run { if (this != it.tenant?.getReference()) unchangeableProperties.add(TENANT) }
            }
        }

    override fun afterPropertiesSet() {
        reseller?.tenant = tenant
        timeZone?.tenant = tenant
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(tenant)
            .add(reseller)
            .add(timeZone)
            .add(folder)
            .toSet()
}
