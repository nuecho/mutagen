package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPReseller
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.UNCHANGED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.GVPResellerReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.TimeZoneReference
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject
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
    val startDate: Date? = null,
    val displayName: String? = null,
    val isParentNSP: Boolean? = CfgFlag.CFGFalse.asBoolean(),
    val notes: String? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null

) : ConfigurationObject, InitializingBean {

    @get:JsonIgnore
    override val reference = GVPResellerReference(name, tenant)

    constructor(gvpReseller: CfgGVPReseller) : this(
        tenant = TenantReference(gvpReseller.tenant.name),
        name = gvpReseller.name,
        displayName = gvpReseller.displayName,
        startDate = gvpReseller.startDate?.time,
        isParentNSP = gvpReseller.isParentNSP?.asBoolean(),
        timeZone = gvpReseller.timeZone?.getReference(),
        notes = gvpReseller.notes,
        state = gvpReseller.state?.toShortName(),
        userProperties = gvpReseller.userProperties?.asCategorizedProperties()
    )

    override fun updateCfgObject(service: IConfService): ConfigurationObjectUpdateResult {
        service.retrieveObject(reference)?.let {
            return ConfigurationObjectUpdateResult(UNCHANGED, it)
        }

        CfgGVPReseller(service).let {
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("name", name, it)
            setProperty("displayName", displayName ?: name, it)
            setProperty("notes", notes, it)
            setProperty("timeZoneDBID", service.getObjectDbid(timeZone ?: TimeZoneReference(tenant = tenant)), it)
            setProperty("isParentNSP", ConfigurationObjects.toCfgFlag(isParentNSP), it)

            if (startDate != null) {
                val zoneId = ZoneId.of(timeZone?.primaryKey ?: "GMT", ZoneId.SHORT_IDS)
                val date = GregorianCalendar.from(
                    ZonedDateTime.ofInstant(startDate.toInstant(), zoneId)
                )
                setProperty(
                    "startDate", date, it
                )
            }
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            setProperty("state", ConfigurationObjects.toCfgObjectState(state), it)
            return ConfigurationObjectUpdateResult(CREATED, it)
        }
    }

    override fun afterPropertiesSet() {
        timeZone?.tenant = tenant
    }
}
