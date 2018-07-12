package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPIVRProfile
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgIVRProfileType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.GVPCustomerReference
import com.nuecho.genesys.cli.models.configuration.reference.GVPIVRProfileReference
import com.nuecho.genesys.cli.models.configuration.reference.GVPResellerReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.GregorianCalendar

/**
 * @See https://docs.genesys.com/Documentation/PSDK/8.5.x/ConfigLayerRef/CfgGVPIVRProfile
 */
data class GVPIVRProfile(
    val name: String,
    val tenant: TenantReference? = null,
    val customer: GVPCustomerReference? = null,
    val reseller: GVPResellerReference? = null,
    val displayName: String? = null,
    val type: String? = null,

    val notes: String? = null,
    val description: String? = null,
    val startServiceDate: Date? = null,
    val endServiceDate: Date? = null,
    val isProvisioned: Boolean? = null,
    val tfn: List<String>? = null,
    val status: String? = null,
    val dids: List<DNReference>? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {

    @get:JsonIgnore
    override val reference = GVPIVRProfileReference(name)

    constructor(gvpivrProfile: CfgGVPIVRProfile) : this(
        name = gvpivrProfile.name,
        customer = gvpivrProfile.customer?.getReference(),
        reseller = gvpivrProfile.reseller?.getReference(),
        tenant = gvpivrProfile.tenant.getReference(),
        displayName = gvpivrProfile.displayName,
        type = gvpivrProfile.type?.toShortName(),
        notes = gvpivrProfile.notes,
        description = gvpivrProfile.description,
        startServiceDate = gvpivrProfile.startServiceDate?.time,
        endServiceDate = gvpivrProfile.endServiceDate?.time,
        isProvisioned = gvpivrProfile.isProvisioned?.asBoolean(),
        tfn = gvpivrProfile.tfn?.split(',')?.map { it.trim() }?.toList(),
        status = gvpivrProfile.status,
        dids = gvpivrProfile.diDs?.map { DNReference(it.number, it.switch.name, it.type) },
        state = gvpivrProfile.state?.toShortName(),
        userProperties = gvpivrProfile.userProperties?.asCategorizedProperties(),
        folder = gvpivrProfile.getFolderReference()
    )

    override fun updateCfgObject(service: IConfService) =
        (service.retrieveObject(reference) ?: CfgGVPIVRProfile(service)).also {
            setProperty("name", name, it)
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("customerDBID", service.getObjectDbid(customer), it)
            setProperty(DISPLAY_NAME, displayName, it)
            setProperty("type", toCfgIVRProfileType(type), it)
            setProperty("notes", notes, it)
            setProperty("description", description, it)
            setDateProperty("startServiceDate", startServiceDate, it)
            setDateProperty("endServiceDate", endServiceDate, it)
            setProperty("isProvisioned", toCfgFlag(isProvisioned), it)
            setProperty("tfn", tfn?.joinToString(), it)
            setProperty("status", status, it)
            setProperty("dids", dids?.map { service.getObjectDbid(it) }?.joinToString(), it)

            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            setProperty("state", toCfgObjectState(state), it)
            setFolder(folder, it)
        }

    override fun checkMandatoryProperties(): Set<String> {
        val missingMandatoryProperties = mutableSetOf<String>()

        if (displayName == null)
            missingMandatoryProperties.add(DISPLAY_NAME)
        if (tenant == null)
            missingMandatoryProperties.add(TENANT)

        return missingMandatoryProperties
    }

    override fun afterPropertiesSet() {
        reseller?.tenant = tenant
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(tenant)
            .add(reseller)
            .add(customer)
            .add(dids)
            .add(folder)
            .toSet()
}

fun setDateProperty(field: String, serviceDate: Date?, cfgObject: CfgGVPIVRProfile) {
    // XXX technically, there's no reason why we should stick to GMT.
    // While reseller do have a timeZone, ivr profile do not.
    val zoneId = ZoneId.of("GMT", ZoneId.SHORT_IDS)
    if (serviceDate != null) {
        val date = GregorianCalendar.from(ZonedDateTime.ofInstant(serviceDate.toInstant(), zoneId))
        setProperty(field, date, cfgObject)
    }
}
