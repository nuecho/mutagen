package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgRole
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill

class ConfigurationBuilder {
    private val actionCodes = HashMap<String, ActionCode>()
    private val persons = HashMap<String, Person>()
    private val roles = HashMap<String, Role>()
    private val skills = HashMap<String, Skill>()

    fun add(cfgObject: ICfgObject) =
        when (cfgObject) {
            is CfgActionCode -> ActionCode(cfgObject).run { actionCodes.put(primaryKey, this) }
            is CfgPerson -> Person(cfgObject).run { persons.put(primaryKey, this) }
            is CfgRole -> Role(cfgObject).run { roles.put(primaryKey, this) }
            is CfgSkill -> Skill(cfgObject).run { skills.put(primaryKey, this) }
            else -> false
        }

    fun build() = Configuration(actionCodes, persons, roles, skills)
}
