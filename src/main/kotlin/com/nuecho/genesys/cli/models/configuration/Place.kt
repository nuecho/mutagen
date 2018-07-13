package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.PlaceReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName

class Place(
    val tenant: TenantReference,
    val name: String,
    val dns: List<DNReference>? = null,
    val capacityRule: ScriptReference? = null,
    val contract: ObjectiveTableReference? = null,
    val site: FolderReference? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = PlaceReference(name, tenant)

    constructor(place: CfgPlace) : this(
        tenant = TenantReference(place.tenant.name),
        name = place.name,
        dns = place.dNs?.map { it.getReference() },
        capacityRule = place.capacityRule?.getReference(),
        contract = place.contract?.getReference(),
        site = place.site?.getReference(),
        state = place.state?.toShortName(),
        userProperties = place.userProperties?.asCategorizedProperties(),
        folder = place.getFolderReference()
    )

    override fun updateCfgObject(service: IConfService) =
        (service.retrieveObject(reference) ?: CfgPlace(service)).also {
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("name", name, it)
            setProperty("DNDBIDs", dns?.map { service.getObjectDbid(it) } ?: emptyList<Int>(), it)
            setProperty("capacityRuleDBID", service.getObjectDbid(capacityRule), it)
            setProperty("contractDBID", service.getObjectDbid(contract), it)
            setProperty("siteDBID", service.getObjectDbid(site), it)
            setProperty("userProperties", ConfigurationObjects.toKeyValueCollection(userProperties), it)
            setProperty("state", ConfigurationObjects.toCfgObjectState(state), it)
            setFolder(folder, it)
        }

    override fun afterPropertiesSet() {
        dns?.forEach { it.updateTenantReferences(tenant) }
        capacityRule?.tenant = tenant
        contract?.tenant = tenant
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(tenant)
            .add(dns)
            .add(capacityRule)
            .add(contract)
            .add(site)
            .add(folder)
            .toSet()
}
