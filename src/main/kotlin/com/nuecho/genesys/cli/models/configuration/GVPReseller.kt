package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPReseller
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.GVPResellerReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.TimeZoneReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.GregorianCalendar

/**
 * @See https://docs.genesys.com/Documentation/PSDK/8.5.x/ConfigLayerRef/CfgGVPReseller
 */
data class GVPReseller(
    val tenant: TenantReference,
    val name: String,
    val timeZone: TimeZoneReference? = null,
    val startDate: Date? = null, // FIXME we lose milliseconds somewhere down the line might be jackson or the confserv
    val displayName: String? = null,
    @get:JsonProperty("isParentNSP")
    val isParentNSP: Boolean? = null,
    val notes: String? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {

    @get:JsonIgnore
    override val reference = GVPResellerReference(name, tenant)

    constructor(gvpReseller: CfgGVPReseller) : this(
        tenant = gvpReseller.tenant.getReference(),
        name = gvpReseller.name,
        displayName = gvpReseller.displayName,
        startDate = gvpReseller.startDate?.time,
        isParentNSP = gvpReseller.isParentNSP?.asBoolean(),
        timeZone = gvpReseller.timeZone?.getReference(),
        notes = gvpReseller.notes,
        state = gvpReseller.state?.toShortName(),
        userProperties = gvpReseller.userProperties?.asCategorizedProperties(),
        folder = gvpReseller.getFolderReference()
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgGVPReseller(service)).also {
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("name", name, it)
        }

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgGVPReseller).also {
            setProperty(DISPLAY_NAME, displayName ?: name, it)
            setProperty("notes", notes, it)
            setProperty("timeZoneDBID", service.getObjectDbid(timeZone ?: TimeZoneReference(tenant = tenant)), it)
            setProperty(IS_PARENT_NSP, toCfgFlag(isParentNSP ?: false), it)

            if (startDate != null) {
                val zoneId = ZoneId.of(timeZone?.primaryKey ?: "GMT", ZoneId.SHORT_IDS)
                val date = GregorianCalendar.from(
                    ZonedDateTime.ofInstant(startDate.toInstant(), zoneId)
                )
                setProperty("startDate", date, it)
            }
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            setProperty("state", toCfgObjectState(state), it)
            setFolder(folder, it)
        }

    override fun cloneBare() = GVPReseller(tenant, name)

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> = emptySet()

    override fun checkUnchangeableProperties(cfgObject: CfgObject) = emptySet<String>()

    override fun afterPropertiesSet() {
        timeZone?.tenant = tenant
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(tenant)
            .add(timeZone)
            .add(folder)
            .toSet()
}
