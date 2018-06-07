package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgEnumeratorType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.EnumeratorReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName

data class Enumerator(
    val tenant: TenantReference,
    val name: String,
    val displayName: String? = null,
    val description: String? = null,
    val type: String? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null
) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = EnumeratorReference(name, tenant)

    constructor(enumerator: CfgEnumerator) : this(
        tenant = TenantReference(enumerator.tenant.name),
        name = enumerator.name,
        displayName = enumerator.displayName,
        description = enumerator.description,
        type = enumerator.type?.toShortName(),
        state = enumerator.state?.toShortName(),
        userProperties = enumerator.userProperties?.asCategorizedProperties()
    )

    override fun updateCfgObject(service: IConfService) =
        (service.retrieveObject(reference) ?: CfgEnumerator(service)).also {
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("name", name, it)
            setProperty("displayName", displayName ?: name, it)
            setProperty("description", description, it)
            setProperty("type", toCfgEnumeratorType(type), it)
            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", ConfigurationObjects.toKeyValueCollection(userProperties), it)
        }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> = setOf(tenant)
}
