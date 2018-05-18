package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.UNCHANGED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName

data class Tenant(
    val name: String,
    val chargeableNumber: String? = null,
    val defaultContract: ObjectiveTableReference? = null,
    val defaultCapacityRule: ScriptReference? = null,
    val isServiceProvider: Boolean? = false,
    val parentTenant: TenantReference? = null,
    val password: String? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null

) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = TenantReference(name)

    constructor(tenant: CfgTenant) : this(
        chargeableNumber = tenant.chargeableNumber,
        defaultCapacityRule = tenant.defaultCapacityRule?.getReference(),
        defaultContract = tenant.defaultContract?.getReference(),
        isServiceProvider = tenant.isServiceProvider.asBoolean(),
        name = tenant.name,
        parentTenant = tenant.parentTenant?.getReference(),
        password = tenant.password,
        state = tenant.state?.toShortName(),
        userProperties = tenant.userProperties?.asCategorizedProperties()
    )

    override fun updateCfgObject(service: IConfService): ConfigurationObjectUpdateResult {
        service.retrieveObject(reference)?.let {
            return ConfigurationObjectUpdateResult(UNCHANGED, it)
        }

        CfgTenant(service).let {
            setProperty("chargeableNumber", chargeableNumber, it)
            setProperty("defaultCapacityRuleDBID", service.getObjectDbid(defaultCapacityRule), it)
            setProperty("defaultContractDBID", service.getObjectDbid(defaultContract), it)
            setProperty("isServiceProvider", toCfgFlag(isServiceProvider), it)
            setProperty("name", name, it)
            setProperty("parentTenantDBID", service.getObjectDbid(parentTenant), it)
            setProperty("password", password, it)

            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            return ConfigurationObjectUpdateResult(CREATED, it)
        }
    }
}
