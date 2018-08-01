package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgTransactionType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.TransactionReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.getObjectDbid
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
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = TransactionReference(name, type, tenant)

    constructor(transaction: CfgTransaction) : this(
        tenant = transaction.tenant.getReference(),
        name = transaction.name,
        type = transaction.type.toShortName(),
        alias = transaction.alias,
        description = transaction.description,
        recordPeriod = transaction.recordPeriod,
        state = transaction.state?.toShortName(),
        userProperties = transaction.userProperties?.asCategorizedProperties(),
        folder = transaction.getFolderReference()
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgTransaction(service).also {
            applyDefaultValues()
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("name", name, it)
            setProperty("type", toCfgTransactionType(type), it)
        })

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgTransaction).also {
            setProperty(ALIAS, alias, it)
            setProperty("description", description, it)
            setProperty("recordPeriod", recordPeriod, it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            setProperty("state", ConfigurationObjects.toCfgObjectState(state), it)
            setFolder(folder, it)
        }

    override fun cloneBare() = null

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> =
        if (alias == null) setOf(ALIAS) else emptySet()

    override fun checkUnchangeableProperties(cfgObject: CfgObject) = emptySet<String>()

    override fun applyDefaultValues() {
        // alias = name
        // description = ""
        // recordPeriod = 0
        // type = CfgTransactionType.CFGTRTNoTransactionType.toShortName()
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(tenant)
            .add(folder)
            .toSet()
}
