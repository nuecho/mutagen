package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import java.util.TreeSet

class ConfigurationBuilder {
    private val persons = TreeSet<Person>()
    private val skills = TreeSet<Skill>()

    fun add(cfgObject: ICfgObject) =
        when (cfgObject) {
            is CfgPerson -> persons.add(Person(cfgObject))
            is CfgSkill -> skills.add(Skill(cfgObject))
            else -> false
        }

    fun build() = Configuration(persons, skills)
}
