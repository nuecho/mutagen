package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgGroup
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.StatTableReference
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

data class Group(
    val name: String,
    val managers: List<PersonReference>? = null,
    val routeDNs: List<DNReference>? = null,
    val capacityTable: StatTableReference? = null,
    val quotaTable: StatTableReference? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    val userProperties: CategorizedProperties? = null,
    val capacityRule: ScriptReference? = null,
    val site: FolderReference? = null,
    val contract: ObjectiveTableReference? = null
) {
    @JsonIgnore
    val reference = name

    constructor(group: CfgGroup) : this(
        name = group.name,
        managers = group.managers?.map { it.getReference() },
        routeDNs = group.routeDNs?.map { it.getReference() },
        capacityTable = group.capacityTable?.getReference(),
        quotaTable = group.quotaTable?.getReference(),
        state = group.state?.toShortName(),
        userProperties = group.userProperties?.asCategorizedProperties(),
        capacityRule = group.capacityRule?.getReference(),
        site = group.site?.getReference(),
        contract = group.contract?.getReference()
    )

    fun toCfgGroup(service: IConfService, parent: CfgObject): CfgGroup =
        CfgGroup(service, parent).also {
            ConfigurationObjects.setProperty("name", name, it)
            ConfigurationObjects.setProperty("managerDBIDs", managers?.mapNotNull { service.getObjectDbid(it) }, it)
            ConfigurationObjects.setProperty("routeDNDBIDs", routeDNs?.mapNotNull { service.getObjectDbid(it) }, it)
            ConfigurationObjects.setProperty("capacityTableDBID", service.getObjectDbid(capacityTable), it)
            ConfigurationObjects.setProperty("quotaTableDBID", service.getObjectDbid(quotaTable), it)
            ConfigurationObjects.setProperty("state", ConfigurationObjects.toCfgObjectState(state), it)
            ConfigurationObjects.setProperty(
                "userProperties",
                ConfigurationObjects.toKeyValueCollection(userProperties), it
            )
            ConfigurationObjects.setProperty("capacityRuleDBID", service.getObjectDbid(capacityRule), it)
            ConfigurationObjects.setProperty("siteDBID", service.getObjectDbid(site), it)
            ConfigurationObjects.setProperty("contractDBID", service.getObjectDbid(contract), it)
        }
}
