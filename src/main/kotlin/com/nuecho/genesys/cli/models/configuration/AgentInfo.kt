package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkillLevel
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.PlaceReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.SimpleObjectReferenceKeySerializer
import com.nuecho.genesys.cli.models.configuration.reference.SimpleObjectReferenceWithTenantKeyDeserializer
import com.nuecho.genesys.cli.models.configuration.reference.SkillReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.getObjectDbid

data class AgentInfo(
    val capacityRule: ScriptReference? = null,
    val contract: ObjectiveTableReference? = null,
    val place: PlaceReference? = null,
    val site: FolderReference? = null,
    @JsonDeserialize(keyUsing = SimpleObjectReferenceWithTenantKeyDeserializer::class)
    @JsonSerialize(keyUsing = SimpleObjectReferenceKeySerializer::class)
    val skillLevels: Map<SkillReference, Int>? = null,
    val agentLogins: List<AgentLoginInfo>? = null
) {
    constructor(agentInfo: CfgAgentInfo) : this(
        capacityRule = agentInfo.capacityRule?.getReference(),
        contract = agentInfo.contract?.getReference(),
        place = agentInfo.place?.getReference(),
        site = agentInfo.site?.getReference(),
        skillLevels = agentInfo.skillLevels?.map { it.skill!!.getReference() to it.level }?.toMap(),
        agentLogins = agentInfo.agentLogins?.map { AgentLoginInfo(it) }
    )

    fun toUpdatedCfgAgentInfo(service: IConfService, agentInfo: CfgAgentInfo) = agentInfo.also {
        // agentLogins are not exported
        setProperty("capacityRuleDBID", service.getObjectDbid(capacityRule), it)
        setProperty("contractDBID", service.getObjectDbid(contract), it)
        setProperty("placeDBID", service.getObjectDbid(place), it)
        setProperty("siteDBID", service.getObjectDbid(site), it)
        setProperty("skillLevels", toCfgSkillLevelList(skillLevels, it.parent as CfgPerson), it)
    }

    @JsonIgnore
    @Suppress("DataClassContainsFunctions")
    fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(capacityRule)
            .add(contract)
            .add(place)
            .add(site)
            .add(skillLevels?.keys)
            .add(agentLogins?.flatMap { it.getReferences() })
            .toSet()
}

fun AgentInfo.updateTenantReferences(tenant: TenantReference) {
    capacityRule?.tenant = tenant
    contract?.tenant = tenant
    place?.tenant = tenant
    skillLevels?.forEach { it.key.tenant = tenant }
    agentLogins?.forEach { it.updateTenantReferences(tenant) }
}

private fun toCfgSkillLevelList(
    skillLevels: Map<SkillReference, Int>?,
    person: CfgPerson
): List<CfgSkillLevel?>? {
    if (skillLevels == null) return null

    val service = person.configurationService

    return skillLevels.mapNotNull { (skillReference, skillLevel) ->
        val skillDbid = service.getObjectDbid(skillReference)
        if (skillDbid == null) null
        else CfgSkillLevel(service, person).apply {
            skillDBID = skillDbid
            level = skillLevel
        }
    }.toList()
}
