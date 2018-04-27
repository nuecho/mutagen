package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkillLevel
import com.nuecho.genesys.cli.Logging.warn
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.services.getFolderDbid
import com.nuecho.genesys.cli.services.getObjectiveTableDbid
import com.nuecho.genesys.cli.services.getPlaceDbid
import com.nuecho.genesys.cli.services.getScriptDbid
import com.nuecho.genesys.cli.services.retrieveSkill
import com.nuecho.genesys.cli.getPrimaryKey

data class AgentInfo(
    val capacityRule: String? = null,
    val contract: String? = null,
    val place: String? = null,
    val site: String? = null,
    val skillLevels: Map<String, Int>? = null,
    val agentLogins: List<AgentLoginInfo>? = null
) {
    constructor(agentInfo: CfgAgentInfo) : this(
        capacityRule = agentInfo.capacityRule?.getPrimaryKey(),
        contract = agentInfo.contract?.getPrimaryKey(),
        place = agentInfo.place?.getPrimaryKey(),
        site = agentInfo.site?.getPrimaryKey(),
        skillLevels = agentInfo.skillLevels?.map { it.skill!!.getPrimaryKey() to it.level }?.toMap(),
        agentLogins = agentInfo.agentLogins?.map { AgentLoginInfo(it) }
    )

    fun toCfgAgentInfo(person: CfgPerson): CfgAgentInfo {
        val service = person.configurationService
        val agentInfo = CfgAgentInfo(person.configurationService, person)

        // agentLogins are not exported
        setProperty("capacityRuleDBID", service.getScriptDbid(capacityRule), agentInfo)
        setProperty("contractDBID", service.getObjectiveTableDbid(contract), agentInfo)
        setProperty("placeDBID", service.getPlaceDbid(contract), agentInfo)
        setProperty("siteDBID", service.getFolderDbid(site), agentInfo)
        setProperty("skillLevels", toCfgSkillLevel(skillLevels, person), agentInfo)

        return agentInfo
    }
}

private fun toCfgSkillLevel(skillLevels: Map<String, Int>?, person: CfgPerson): List<CfgSkillLevel?>? {
    if (skillLevels == null) return null

    val service = person.configurationService

    return skillLevels.map { (name, level) ->
        val cfgSkill = service.retrieveSkill(name)
        if (cfgSkill == null) {
            warn { "Cannot find skill '$name'" }
            null
        } else {
            val cfgSkillLevel = CfgSkillLevel(service, person)
            cfgSkillLevel.skillDBID = cfgSkill.dbid
            cfgSkillLevel.level = level
            cfgSkillLevel
        }
    }.filter { it != null }.toList()
}
