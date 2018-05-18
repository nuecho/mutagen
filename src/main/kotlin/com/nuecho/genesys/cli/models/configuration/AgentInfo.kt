package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkillLevel
import com.nuecho.genesys.cli.Logging.warn
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.PlaceReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.SimpleObjectReferenceKeyDeserializer
import com.nuecho.genesys.cli.models.configuration.reference.SimpleObjectReferenceKeySerializer
import com.nuecho.genesys.cli.models.configuration.reference.SkillReference
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject

data class AgentInfo(
    val capacityRule: ScriptReference? = null,
    val contract: ObjectiveTableReference? = null,
    val place: PlaceReference? = null,
    val site: FolderReference? = null,
    @JsonDeserialize(keyUsing = SimpleObjectReferenceKeyDeserializer::class)
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

    fun toCfgAgentInfo(person: CfgPerson): CfgAgentInfo {
        val service = person.configurationService
        val agentInfo = CfgAgentInfo(person.configurationService, person)

        // agentLogins are not exported
        setProperty("capacityRuleDBID", service.getObjectDbid(capacityRule), agentInfo)
        setProperty("contractDBID", service.getObjectDbid(contract), agentInfo)
        setProperty("placeDBID", service.getObjectDbid(place), agentInfo)
        setProperty("siteDBID", service.getObjectDbid(site), agentInfo)
        setProperty("skillLevels", toCfgSkillLevel(skillLevels, person), agentInfo)

        return agentInfo
    }
}

private fun toCfgSkillLevel(skillLevels: Map<SkillReference, Int>?, person: CfgPerson): List<CfgSkillLevel?>? {
    if (skillLevels == null) return null

    val service = person.configurationService

    return skillLevels.mapNotNull { (reference, level) ->
        val cfgSkill = service.retrieveObject(reference)
        if (cfgSkill == null) {
            warn { "Cannot find skill '$reference'" }
            null
        } else {
            val cfgSkillLevel = CfgSkillLevel(service, person)
            cfgSkillLevel.skillDBID = cfgSkill.dbid
            cfgSkillLevel.level = level
            cfgSkillLevel
        }
    }.toList()
}
