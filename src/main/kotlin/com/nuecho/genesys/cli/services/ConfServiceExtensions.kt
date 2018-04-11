package com.nuecho.genesys.cli.services

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgRole
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction
import com.genesyslab.platform.applicationblocks.com.queries.CfgActionCodeQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgAgentLoginQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgEnumeratorQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgFolderQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgObjectiveTableQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPersonQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPlaceQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgRoleQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgScriptQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgSkillQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgSwitchQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgTenantQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgTransactionQuery
import com.nuecho.genesys.cli.Logging

fun IConfService.retrieveActionCode(name: String): CfgActionCode? =
    retrieveObject(CfgActionCode::class.java, CfgActionCodeQuery(name))

fun IConfService.retrieveAgentLogin(loginCode: String): CfgAgentLogin? {
    val query = CfgAgentLoginQuery()
    query.loginCode = loginCode
    return retrieveObject(CfgAgentLogin::class.java, query)
}

fun IConfService.retrieveEnumerator(name: String): CfgEnumerator? =
    retrieveObject(CfgEnumerator::class.java, CfgEnumeratorQuery(name))

fun IConfService.retrieveFolder(name: String): CfgFolder? = retrieveObject(CfgFolder::class.java, CfgFolderQuery(name))

fun IConfService.retrieveObjectiveTable(name: String): CfgObjectiveTable? =
    retrieveObject(CfgObjectiveTable::class.java, CfgObjectiveTableQuery(name))

fun IConfService.retrievePerson(employeeId: String): CfgPerson? {
    val query = CfgPersonQuery()
    query.employeeId = employeeId
    return retrieveObject(CfgPerson::class.java, query)
}

fun IConfService.retrievePlace(name: String): CfgPlace? = retrieveObject(CfgPlace::class.java, CfgPlaceQuery(name))

fun IConfService.retrieveRole(name: String): CfgRole? = retrieveObject(CfgRole::class.java, CfgRoleQuery(name))

fun IConfService.retrieveScript(name: String): CfgScript? = retrieveObject(CfgScript::class.java, CfgScriptQuery(name))

fun IConfService.retrieveSkill(name: String): CfgSkill? = retrieveObject(CfgSkill::class.java, CfgSkillQuery(name))

fun IConfService.retrieveTransaction(name: String): CfgTransaction? =
    retrieveObject(CfgTransaction::class.java, CfgTransactionQuery(name))

fun IConfService.retrieveSwitch(name: String): CfgSwitch? = retrieveObject(CfgSwitch::class.java, CfgSwitchQuery(name))
fun IConfService.retrieveTenant(name: String): CfgTenant? = retrieveObject(CfgTenant::class.java, CfgTenantQuery(name))
fun IConfService.retrieveTenants(): Collection<CfgTenant> =
    retrieveMultipleObjects(CfgTenant::class.java, CfgTenantQuery().apply { allTenants = 1 })

val IConfService.tenants: Collection<CfgTenant> by TenantCacheDelegate()

val IConfService.defaultTenantDbid: Int
    get() {
        if (tenants.isEmpty()) throw IllegalStateException("No tenant found.")
        if (tenants.size > 1) throw IllegalStateException("Unsupported multi-tenancy.")
        return tenants.first().dbid
    }

fun IConfService.getTenantDbid(name: String?) =
    if (name == null) null
    else {
        val cfgTenant = retrieveTenant(name)

        if (cfgTenant == null) {
            Logging.warn { "Cannot find tenant '$name'" }
            null
        } else cfgTenant.dbid
    }

fun IConfService.getScriptDbid(name: String?) =
    if (name == null) null
    else {
        val cfgScript = retrieveScript(name)

        if (cfgScript == null) {
            Logging.warn { "Cannot find script '$name'" }
            null
        } else cfgScript.dbid
    }

fun IConfService.getObjectiveTableDbid(name: String?) =
    if (name == null) null
    else {
        val cfgObjectiveTable = retrieveObjectiveTable(name)

        if (cfgObjectiveTable == null) {
            Logging.warn { "Cannot find objective table '$name'" }
            null
        } else cfgObjectiveTable.dbid
    }
