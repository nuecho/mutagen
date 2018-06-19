package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgTransactionType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.TransactionReference
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName

data class Transaction(
    val tenant: TenantReference,
    val name: String,
    val type: String,
    val alias: String? = null,
    val description: String? = null,
    val recordPeriod: Int? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null

) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = TransactionReference(name, type, tenant)

    constructor(transaction: CfgTransaction) : this(
        tenant = TenantReference(transaction.tenant.name),
        name = transaction.name,
        alias = transaction.alias,
        description = transaction.description,
        recordPeriod = transaction.recordPeriod,
        state = transaction.state?.toShortName(),
        type = transaction.type.toShortName(),
        userProperties = transaction.userProperties?.asCategorizedProperties()
    )

    override fun updateCfgObject(service: IConfService) =
        (service.retrieveObject(reference) ?: CfgTransaction(service)).also {
            if (!it.isSaved) {
                applyDefaultValues()
            }

            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("alias", alias, it)
            setProperty("description", description, it)
            setProperty("recordPeriod", recordPeriod, it)
            setProperty("type", toCfgTransactionType(type), it)

            setProperty("name", name, it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            setProperty("state", ConfigurationObjects.toCfgObjectState(state), it)
        }

    override fun applyDefaultValues() {
        // alias = name
        // description = ""
        // recordPeriod = 0
        // type = CfgTransactionType.CFGTRTNoTransactionType.toShortName()
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> = setOf(tenant)
}
