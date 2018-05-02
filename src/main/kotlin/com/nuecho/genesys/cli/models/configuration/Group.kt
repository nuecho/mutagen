package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgGroup
import com.nuecho.genesys.cli.getPrimaryKey
import com.nuecho.genesys.cli.services.getDNDbid
import com.nuecho.genesys.cli.services.getFolderDbid
import com.nuecho.genesys.cli.services.getObjectiveTableDbid
import com.nuecho.genesys.cli.services.getPersonDbid
import com.nuecho.genesys.cli.services.getScriptDbid
import com.nuecho.genesys.cli.services.getStatTableDbid
import com.nuecho.genesys.cli.toPrimaryKeyList
import com.nuecho.genesys.cli.toShortName

data class Group(
    val name: String,
    val managers: List<String>? = null,
    val routeDNs: List<String>? = null,
    val capacityTable: String? = null,
    val quotaTable: String? = null,
    val state: String? = null,
    @JsonSerialize(using = KeyValueCollectionSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    val userProperties: CategorizedProperties? = null,
    val capacityRule: String? = null,
    val site: String? = null,
    val contract: String? = null
) {
    @JsonIgnore
    val primaryKey: String = name

    constructor(group: CfgGroup) : this(
        name = group.name,
        managers = group.managers?.toPrimaryKeyList(),
        routeDNs = group.routeDNs?.toPrimaryKeyList(),
        capacityTable = group.capacityTable?.getPrimaryKey(),
        quotaTable = group.quotaTable?.getPrimaryKey(),
        state = group.state?.toShortName(),
        userProperties = group.userProperties?.asCategorizedProperties(),
        capacityRule = group.capacityRule?.getPrimaryKey(),
        site = group.site?.getPrimaryKey(),
        contract = group.contract?.getPrimaryKey()
    )

    fun toCfgGroup(service: IConfService, parent: CfgObject): CfgGroup =
        CfgGroup(service, parent).also {
            ConfigurationObjects.setProperty("name", name, it)
            ConfigurationObjects.setProperty("managerDBIDs", managers?.map { service.getPersonDbid(it) }, it)
            ConfigurationObjects.setProperty("routeDNDBIDs", routeDNs?.map { service.getDNDbid(it) }, it)
            ConfigurationObjects.setProperty("capacityTableDBID", service.getStatTableDbid(capacityTable), it)
            ConfigurationObjects.setProperty("quotaTableDBID", service.getStatTableDbid(quotaTable), it)
            ConfigurationObjects.setProperty("state", ConfigurationObjects.toCfgObjectState(state), it)
            ConfigurationObjects.setProperty(
                "userProperties",
                ConfigurationObjects.toKeyValueCollection(userProperties), it
            )
            ConfigurationObjects.setProperty("capacityRuleDBID", service.getScriptDbid(capacityRule), it)
            ConfigurationObjects.setProperty("siteDBID", service.getFolderDbid(site), it)
            ConfigurationObjects.setProperty("contractDBID", service.getObjectiveTableDbid(contract), it)
        }
}
