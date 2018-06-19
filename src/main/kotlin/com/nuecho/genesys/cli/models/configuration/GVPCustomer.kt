package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPCustomer
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.GVPCustomerReference
import com.nuecho.genesys.cli.models.configuration.reference.GVPResellerReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.TimeZoneReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject
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
    val isProvisioned: Boolean? = null,
    val isAdminCustomer: Boolean? = null,
    val timeZone: TimeZoneReference? = null,

    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null

) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = GVPCustomerReference(name)

    constructor(gvpCustomer: CfgGVPCustomer) : this(
        name = gvpCustomer.name,
        tenant = TenantReference(gvpCustomer.tenant.name),
        reseller = gvpCustomer.reseller.getReference(),

        channel = gvpCustomer.channel,
        displayName = gvpCustomer.displayName,
        notes = gvpCustomer.notes,
        isProvisioned = gvpCustomer.isProvisioned.asBoolean(),
        isAdminCustomer = gvpCustomer.isAdminCustomer.asBoolean(),

        timeZone = gvpCustomer.timeZone?.getReference(),

        state = gvpCustomer.state?.toShortName(),
        userProperties = gvpCustomer.userProperties?.asCategorizedProperties()
    )

    override fun updateCfgObject(service: IConfService) =
        (service.retrieveObject(reference) ?: CfgGVPCustomer(service)).also {
            setProperty("name", name, it)
            setProperty("channel", channel, it)
            setProperty("displayName", displayName ?: name, it)
            setProperty("notes", notes, it)
            setProperty("isProvisioned", toCfgFlag(isProvisioned ?: false), it)
            setProperty("isAdminCustomer", toCfgFlag(isAdminCustomer ?: false), it)

            setProperty("timeZoneDBID", service.getObjectDbid(timeZone), it)
            setProperty("resellerDBID", service.getObjectDbid(reseller), it)

            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            setProperty("state", toCfgObjectState(state), it)
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
            .toSet()
}
