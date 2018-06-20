package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumeratorValue
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGEnumerator
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.EnumeratorReference
import com.nuecho.genesys.cli.models.configuration.reference.EnumeratorValueReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName

data class EnumeratorValue(
    val tenant: TenantReference? = null,
    val default: Boolean,
    val displayName: String,
    val enumerator: EnumeratorReference,
    val name: String,
    val description: String? = null,
    val state: String? = null,

    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null
) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = EnumeratorValueReference(name, enumerator)

    constructor(enumeratorValue: CfgEnumeratorValue) : this(
        tenant = TenantReference(enumeratorValue.tenant.name),
        default = enumeratorValue.isDefault.asBoolean()!!,
        description = enumeratorValue.description,
        displayName = enumeratorValue.displayName,
        enumerator = enumeratorValue.enumeratorDBID.let {
            val enumerator = enumeratorValue.configurationService.retrieveObject(CFGEnumerator, it) as CfgEnumerator
            EnumeratorReference(enumerator.name, TenantReference(enumeratorValue.tenant.name))
        },
        name = enumeratorValue.name,
        state = enumeratorValue.state.toShortName(),
        userProperties = enumeratorValue.userProperties?.asCategorizedProperties()
    )

    constructor(tenant: TenantReference, default: Boolean, displayName: String, enumerator: String, name: String) :
            this(
                tenant = tenant,
                default = default,
                displayName = displayName,
                enumerator = EnumeratorReference(enumerator, tenant),
                name = name
            )

    override fun updateCfgObject(service: IConfService) =
        (service.retrieveObject(reference) ?: CfgEnumeratorValue(service)).also { cfgEnumeratorValue ->
            if (!cfgEnumeratorValue.isSaved) {
                applyDefaultValues()
            }

            setProperty("tenantDBID", service.getObjectDbid(tenant), cfgEnumeratorValue)
            setProperty("enumeratorDBID", service.getObjectDbid(enumerator), cfgEnumeratorValue)
            setProperty("name", name, cfgEnumeratorValue)
            setProperty("description", description, cfgEnumeratorValue)
            setProperty("displayName", displayName, cfgEnumeratorValue)
            setProperty("isDefault", ConfigurationObjects.toCfgFlag(default), cfgEnumeratorValue)
            setProperty("state", toCfgObjectState(state), cfgEnumeratorValue)
            setProperty("userProperties", ConfigurationObjects.toKeyValueCollection(userProperties), cfgEnumeratorValue)
        }

    override fun afterPropertiesSet() {
        enumerator.tenant = tenant
    }

    override fun applyDefaultValues() {
//        default = false
//        displayName = name
//        state = "enabled"
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(enumerator)
            .toSet()
}
