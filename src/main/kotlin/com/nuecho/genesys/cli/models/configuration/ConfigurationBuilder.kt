package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgRole
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import java.util.TreeMap

class ConfigurationBuilder {
    private val persons = TreeMap<String, Person>()
    private val roles = TreeMap<String, Role>()
    private val skills = TreeMap<String, Skill>()

    fun add(cfgObject: ICfgObject) =
        when (cfgObject) {
            is CfgPerson -> Person(cfgObject).let { persons.put(it.primaryKey, it) }
            is CfgRole -> Role(cfgObject).let { roles.put(it.primaryKey, it) }
            is CfgSkill -> Skill(cfgObject).let { skills.put(it.primaryKey, it) }
            else -> false
        }

    fun build() = Configuration(persons, roles, skills)
}
