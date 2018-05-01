package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction
import com.genesyslab.platform.configuration.protocol.types.CfgTransactionType
import com.nuecho.genesys.cli.asMap
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.UNCHANGED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgTransactionType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.services.retrieveTransaction
import com.nuecho.genesys.cli.toShortName

data class Transaction(
    val name: String,
    val alias: String? = name,
    val description: String? = "",
    val recordPeriod: Int? = 0,
    val state: String? = null,
    val type: String? = CfgTransactionType.CFGTRTNoTransactionType.toShortName(),
    @JsonSerialize(using = KeyValueCollectionSerializer::class)
    @JsonDeserialize(using = KeyValueCollectionDeserializer::class)
    override val userProperties: Map<String, Any>? = null

) : ConfigurationObject {
    override val primaryKey: String
        @JsonIgnore
        get() = name

    constructor(transaction: CfgTransaction) : this(
        name = transaction.name,
        alias = transaction.alias,
        description = transaction.description,
        recordPeriod = transaction.recordPeriod,
        state = transaction.state?.toShortName(),
        type = transaction.type.toShortName(),
        userProperties = transaction.userProperties?.asMap()
    )

    override fun updateCfgObject(service: IConfService): ConfigurationObjectUpdateResult {
        service.retrieveTransaction(name)?.let {
            return ConfigurationObjectUpdateResult(UNCHANGED, it)
        }

        CfgTransaction(service).let {
            setProperty("alias", alias, it)
            setProperty("description", description, it)
            setProperty("recordPeriod", recordPeriod, it)
            setProperty("type", toCfgTransactionType(type), it)

            setProperty("name", name, it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            setProperty("state", ConfigurationObjects.toCfgObjectState(state), it)
            return ConfigurationObjectUpdateResult(CREATED, it)
        }
    }
}