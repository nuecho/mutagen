package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
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
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

data class Tenant(
    val name: String,
    val chargeableNumber: String? = null,
    val defaultContract: ObjectiveTableReference? = null,
    val defaultCapacityRule: ScriptReference? = null,
    val serviceProvider: Boolean? = null,
    val parentTenant: TenantReference? = null,
    val password: String? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = TenantReference(name)

    constructor(tenant: CfgTenant) : this(
        chargeableNumber = tenant.chargeableNumber,
        defaultCapacityRule = tenant.defaultCapacityRule?.getReference(),
        defaultContract = tenant.defaultContract?.getReference(),
        serviceProvider = tenant.isServiceProvider.asBoolean(),
        name = tenant.name,
        parentTenant = tenant.parentTenant?.getReference(),
        password = tenant.password,
        state = tenant.state?.toShortName(),
        userProperties = tenant.userProperties?.asCategorizedProperties(),
        folder = tenant.getFolderReference()
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgTenant(service).also { applyDefaultValues() })

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgTenant).also {

            setProperty("chargeableNumber", chargeableNumber, it)
            setProperty("defaultCapacityRuleDBID", service.getObjectDbid(defaultCapacityRule), it)
            setProperty("defaultContractDBID", service.getObjectDbid(defaultContract), it)
            setProperty("isServiceProvider", toCfgFlag(serviceProvider), it)
            setProperty("name", name, it)
            setProperty("parentTenantDBID", service.getObjectDbid(parentTenant), it)
            setProperty("password", password, it)

            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            setFolder(folder, it)
        }

    override fun cloneBare() = Tenant(name)

    override fun applyDefaultValues() {
        // serviceProvider = false
    }

    override fun afterPropertiesSet() {
        defaultContract?.tenant = reference
        defaultCapacityRule?.tenant = reference
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(defaultContract)
            .add(defaultCapacityRule)
            .add(parentTenant)
            .add(folder)
            .toSet()
}
