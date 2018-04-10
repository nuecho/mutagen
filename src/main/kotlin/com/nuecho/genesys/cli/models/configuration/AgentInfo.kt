package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkillLevel
import com.nuecho.genesys.cli.Logging
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.getPrimaryKey
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.services.retrieveFolder
import com.nuecho.genesys.cli.services.retrieveObjectiveTable
import com.nuecho.genesys.cli.services.retrievePlace
import com.nuecho.genesys.cli.services.retrieveScript
import com.nuecho.genesys.cli.services.retrieveSkill

data class AgentInfo(
    val capacityRule: String? = null,
    val contract: String? = null,
    val place: String? = null,
    val site: String? = null,
    val skillLevels: Map<String, Int>? = null,
    val agentLogins: List<AgentLoginInfo>? = null
) {
    constructor(agentInfo: CfgAgentInfo) : this(
        capacityRule = getPrimaryKey(agentInfo.capacityRule),
        contract = getPrimaryKey(agentInfo.contract),
        place = getPrimaryKey(agentInfo.place),
        site = getPrimaryKey(agentInfo.site),
        skillLevels = agentInfo.skillLevels?.map { getPrimaryKey(it.skill)!! to it.level }?.toMap(),
        agentLogins = agentInfo.agentLogins?.map { AgentLoginInfo(it) }?.toList()
    )

    fun toCfgAgentInfo(person: CfgPerson): CfgAgentInfo {
        val service = person.configurationService
        val agentInfo = CfgAgentInfo(person.configurationService, person)

        // agentLogins are not exported
        setProperty("capacityRuleDBID", getScriptDbid(capacityRule, service), agentInfo)
        setProperty("contractDBID", getObjectiveTableDbid(contract, service), agentInfo)
        setProperty("placeDBID", getPlaceDbid(contract, service), agentInfo)
        setProperty("siteDBID", getFolderDbid(site, service), agentInfo)
        setProperty("skillLevels", toCfgSkillLevel(skillLevels, person), agentInfo)

        return agentInfo
    }
}

private fun getScriptDbid(name: String?, service: IConfService) =
    if (name == null) null
    else {
        val cfgScript = service.retrieveScript(name)

        if (cfgScript == null) {
            Logging.warn { "Cannot find script '$name'" }
            null
        } else cfgScript.dbid
    }

private fun getObjectiveTableDbid(name: String?, service: IConfService) =
    if (name == null) null
    else {
        val cfgObjectiveTable = service.retrieveObjectiveTable(name)

        if (cfgObjectiveTable == null) {
            Logging.warn { "Cannot find objective table '$name'" }
            null
        } else cfgObjectiveTable.dbid
    }

private fun getPlaceDbid(name: String?, service: IConfService) =
    if (name == null) null
    else {
        val cfgPlace = service.retrievePlace(name)

        if (cfgPlace == null) {
            Logging.warn { "Cannot find place '$name'" }
            null
        } else cfgPlace.dbid
    }

private fun getFolderDbid(name: String?, service: IConfService) =
    if (name == null) null
    else {
        val cfgFolder = service.retrieveFolder(name)

        if (cfgFolder == null) {
            Logging.warn { "Cannot find folder '$name'" }
            null
        } else cfgFolder.dbid
    }

private fun toCfgSkillLevel(skillLevels: Map<String, Int>?, person: CfgPerson): List<CfgSkillLevel?>? {
    if (skillLevels == null) return null

    val service = person.configurationService

    return skillLevels.map { (name, level) ->
        val cfgSkill = service.retrieveSkill(name)
        if (cfgSkill == null) {
            Logging.warn { "Cannot find skill '$name'" }
            null
        } else {
            val cfgSkillLevel = CfgSkillLevel(service, person)
            cfgSkillLevel.skillDBID = cfgSkill.dbid
            cfgSkillLevel.level = level
            cfgSkillLevel
        }
    }.filter { it != null }.toList()
}
