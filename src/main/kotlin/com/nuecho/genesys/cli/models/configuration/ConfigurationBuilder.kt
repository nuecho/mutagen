package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import java.util.TreeSet

class ConfigurationBuilder {
    private val persons = TreeSet<Person>()

    fun add(cfgObject: ICfgObject) =
        when (cfgObject) {
            is CfgPerson -> persons.add(Person(cfgObject))
            else -> false
        }

    fun build() = Configuration(persons)
}
