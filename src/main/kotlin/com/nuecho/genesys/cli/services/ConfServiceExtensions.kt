package com.nuecho.genesys.cli.services

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgRole
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.applicationblocks.com.objects.CfgStatTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction
import com.genesyslab.platform.applicationblocks.com.queries.CfgActionCodeQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgAgentGroupQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgAgentLoginQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgApplicationQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgDNQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgEnumeratorQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgFolderQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgObjectiveTableQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPersonQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPhysicalSwitchQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPlaceQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgRoleQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgScriptQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgSkillQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgStatTableQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgSwitchQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgTenantQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgTransactionQuery
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGApplication
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGDN
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGFolder
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGObjectiveTable
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPerson
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPhysicalSwitch
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPlace
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGScript
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGStatTable
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGSwitch
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGTenant
import com.nuecho.genesys.cli.Logging.warn
import com.nuecho.genesys.cli.toShortName

fun IConfService.retrieveActionCode(name: String): CfgActionCode? =
    retrieveObject(CfgActionCode::class.java, CfgActionCodeQuery(name))

fun IConfService.retrieveAgentGroup(name: String): CfgAgentGroup? =
    retrieveObject(CfgAgentGroup::class.java, CfgAgentGroupQuery(name))

fun IConfService.retrieveAgentLogin(loginCode: String): CfgAgentLogin? {
    val query = CfgAgentLoginQuery()
    query.loginCode = loginCode
    return retrieveObject(CfgAgentLogin::class.java, query)
}

fun IConfService.retrieveApplication(name: String): CfgApplication? =
    retrieveObject(CfgApplication::class.java, CfgApplicationQuery(name))

fun IConfService.retrieveDN(dn: String): CfgDN? =
    retrieveObject(CfgDN::class.java, CfgDNQuery(dn))

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

fun IConfService.retrievePhysicalSwitch(name: String): CfgPhysicalSwitch? =
    retrieveObject(CfgPhysicalSwitch::class.java, CfgPhysicalSwitchQuery(name))

fun IConfService.retrievePlace(name: String): CfgPlace? = retrieveObject(CfgPlace::class.java, CfgPlaceQuery(name))

fun IConfService.retrieveRole(name: String): CfgRole? = retrieveObject(CfgRole::class.java, CfgRoleQuery(name))

fun IConfService.retrieveScript(name: String): CfgScript? = retrieveObject(CfgScript::class.java, CfgScriptQuery(name))

fun IConfService.retrieveSkill(name: String): CfgSkill? = retrieveObject(CfgSkill::class.java, CfgSkillQuery(name))

fun IConfService.retrieveStatTable(name: String): CfgStatTable? =
    retrieveObject(CfgStatTable::class.java, CfgStatTableQuery(name))

fun IConfService.retrieveSwitch(name: String): CfgSwitch? = retrieveObject(CfgSwitch::class.java, CfgSwitchQuery(name))

fun IConfService.retrieveTransaction(name: String): CfgTransaction? =
    retrieveObject(CfgTransaction::class.java, CfgTransactionQuery(name))

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

fun IConfService.getApplicationDbid(name: String?) = primaryKeyToDbid(name, ::retrieveApplication, CFGApplication)
fun IConfService.getDNDbid(name: String?) = primaryKeyToDbid(name, ::retrieveDN, CFGDN)
fun IConfService.getFolderDbid(name: String?) = primaryKeyToDbid(name, ::retrieveFolder, CFGFolder)
fun IConfService.getObjectiveTableDbid(name: String?) =
    primaryKeyToDbid(name, ::retrieveObjectiveTable, CFGObjectiveTable)

fun IConfService.getPersonDbid(name: String?) = primaryKeyToDbid(name, ::retrievePerson, CFGPerson)
fun IConfService.getPhysicalSwitchDbid(name: String?) =
    primaryKeyToDbid(name, ::retrievePhysicalSwitch, CFGPhysicalSwitch)

fun IConfService.getPlaceDbid(name: String?) = primaryKeyToDbid(name, ::retrievePlace, CFGPlace)
fun IConfService.getScriptDbid(name: String?) = primaryKeyToDbid(name, ::retrieveScript, CFGScript)
fun IConfService.getStatTableDbid(name: String?) = primaryKeyToDbid(name, ::retrieveStatTable, CFGStatTable)
fun IConfService.getSwitchDbid(name: String?) = primaryKeyToDbid(name, ::retrieveSwitch, CFGSwitch)
fun IConfService.getTenantDbid(name: String?) = primaryKeyToDbid(name, ::retrieveTenant, CFGTenant)

private fun IConfService.primaryKeyToDbid(
    primaryKey: String?,
    retrieve: (String) -> CfgObject?,
    objectType: CfgObjectType
) =
    if (primaryKey == null) null
    else {
        val cfgObject = retrieve(primaryKey)

        if (cfgObject == null) {
            warn { "Cannot find ${objectType.toShortName()} '$primaryKey'" }
            null
        } else cfgObject.objectDbid
    }
