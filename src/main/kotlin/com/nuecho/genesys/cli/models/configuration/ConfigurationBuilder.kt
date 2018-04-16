package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgRole
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction

class ConfigurationBuilder {
    private val actionCodes = HashMap<String, ActionCode>()
    private val enumerators = HashMap<String, Enumerator>()
    private val persons = HashMap<String, Person>()
    private val roles = HashMap<String, Role>()
    private val scripts = HashMap<String, Script>()
    private val skills = HashMap<String, Skill>()
    private val tenants = HashMap<String, Tenant>()
    private val transactions = HashMap<String, Transaction>()

    fun add(cfgObject: ICfgObject) =
        when (cfgObject) {
            is CfgActionCode -> ActionCode(cfgObject).run { actionCodes.put(primaryKey, this) }
            is CfgEnumerator -> Enumerator(cfgObject).run { enumerators.put(primaryKey, this) }
            is CfgPerson -> Person(cfgObject).run { persons.put(primaryKey, this) }
            is CfgRole -> Role(cfgObject).run { roles.put(primaryKey, this) }
            is CfgScript -> Script(cfgObject).run { scripts.put(primaryKey, this) }
            is CfgSkill -> Skill(cfgObject).run { skills.put(primaryKey, this) }
            is CfgTenant -> Tenant(cfgObject).run { tenants.put(primaryKey, this) }
            is CfgTransaction -> Transaction(cfgObject).run { transactions.put(primaryKey, this) }
            else -> false
        }

    fun build() = Configuration(actionCodes, enumerators, persons, roles, scripts, skills, tenants, transactions)
}
