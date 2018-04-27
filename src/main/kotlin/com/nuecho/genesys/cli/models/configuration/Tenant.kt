package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.asMap
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.UNCHANGED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.services.getObjectiveTableDbid
import com.nuecho.genesys.cli.services.getScriptDbid
import com.nuecho.genesys.cli.services.getTenantDbid
import com.nuecho.genesys.cli.services.retrieveTenant
import com.nuecho.genesys.cli.getPrimaryKey
import com.nuecho.genesys.cli.toShortName

data class Tenant(
    val name: String,
    val chargeableNumber: String? = null,
    val defaultContract: String? = null,
    val defaultCapacityRule: String? = null,
    val isServiceProvider: Boolean? = false,
    val parentTenant: String? = null,
    val password: String? = null,
    val state: String? = null,
    @JsonSerialize(using = KeyValueCollectionSerializer::class)
    @JsonDeserialize(using = KeyValueCollectionDeserializer::class)
    override val userProperties: Map<String, Any>? = null

) : ConfigurationObject {
    override val primaryKey: String
        @JsonIgnore
        get() = name

    constructor(tenant: CfgTenant) : this(
        chargeableNumber = tenant.chargeableNumber,
        defaultCapacityRule = tenant.defaultCapacityRule?.getPrimaryKey(),
        defaultContract = tenant.defaultContract?.getPrimaryKey(),
        isServiceProvider = tenant.isServiceProvider.asBoolean(),
        name = tenant.name,
        parentTenant = tenant.parentTenant?.getPrimaryKey(),
        password = tenant.password,
        state = tenant.state?.toShortName(),
        userProperties = tenant.userProperties?.asMap()
    )

    override fun updateCfgObject(service: IConfService): ConfigurationObjectUpdateResult {
        service.retrieveTenant(name)?.let {
            return ConfigurationObjectUpdateResult(UNCHANGED, it)
        }

        CfgTenant(service).let {
            setProperty("chargeableNumber", chargeableNumber, it)
            setProperty("defaultCapacityRuleDBID", service.getScriptDbid(defaultCapacityRule), it)
            setProperty("defaultContractDBID", service.getObjectiveTableDbid(defaultContract), it)
            setProperty("isServiceProvider", toCfgFlag(isServiceProvider), it)
            setProperty("name", name, it)
            setProperty("parentTenantDBID", service.getTenantDbid(parentTenant), it)
            setProperty("password", password, it)

            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            return ConfigurationObjectUpdateResult(CREATED, it)
        }
    }
}
