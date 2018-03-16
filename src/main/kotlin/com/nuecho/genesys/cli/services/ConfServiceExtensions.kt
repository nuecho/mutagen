package com.nuecho.genesys.cli.services

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.queries.CfgAgentLoginQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgFolderQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgObjectiveTableQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPersonQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPlaceQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgScriptQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgSkillQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgSwitchQuery

fun IConfService.retrievePerson(employeeId: String): CfgPerson? {
    val query = CfgPersonQuery()
    query.employeeId = employeeId
    return retrieveObject(CfgPerson::class.java, query)
}

fun IConfService.retrieveAgentLogin(loginCode: String): CfgAgentLogin? {
    val query = CfgAgentLoginQuery()
    query.loginCode = loginCode
    return retrieveObject(CfgAgentLogin::class.java, query)
}

fun IConfService.retrieveScript(name: String): CfgScript? = retrieveObject(CfgScript::class.java, CfgScriptQuery(name))
fun IConfService.retrieveFolder(name: String): CfgFolder? = retrieveObject(CfgFolder::class.java, CfgFolderQuery(name))
fun IConfService.retrievePlace(name: String): CfgPlace? = retrieveObject(CfgPlace::class.java, CfgPlaceQuery(name))
fun IConfService.retrieveSkill(name: String): CfgSkill? = retrieveObject(CfgSkill::class.java, CfgSkillQuery(name))
fun IConfService.retrieveSwitch(name: String): CfgSwitch? = retrieveObject(CfgSwitch::class.java, CfgSwitchQuery(name))
fun IConfService.retrieveObjectiveTable(name: String): CfgObjectiveTable? =
    retrieveObject(CfgObjectiveTable::class.java, CfgObjectiveTableQuery(name))
