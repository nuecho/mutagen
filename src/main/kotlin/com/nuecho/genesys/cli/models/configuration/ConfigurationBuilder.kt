package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgAccessGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgAppPrototype
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumeratorValue
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPCustomer
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPIVRProfile
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPReseller
import com.genesyslab.platform.applicationblocks.com.objects.CfgHost
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgRole
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction

class ConfigurationBuilder {
    private val accessGroups = ArrayList<AccessGroup>()
    private val actionCodes = ArrayList<ActionCode>()
    private val agentGroups = ArrayList<AgentGroup>()
    private val appPrototypes = ArrayList<AppPrototype>()
    private val dnGroups = ArrayList<DNGroup>()
    private val dns = ArrayList<DN>()
    private val enumerators = ArrayList<Enumerator>()
    private val enumeratorValues = ArrayList<EnumeratorValue>()
    private val folders = ArrayList<Folder>()
    private val gvpCustomers = ArrayList<GVPCustomer>()
    private val gvpResellers = ArrayList<GVPReseller>()
    private val gvpIVRProfiles = ArrayList<GVPIVRProfile>()
    private val hosts = ArrayList<Host>()
    private val persons = ArrayList<Person>()
    private val places = ArrayList<Place>()
    private val physicalSwitches = ArrayList<PhysicalSwitch>()
    private val roles = ArrayList<Role>()
    private val scripts = ArrayList<Script>()
    private val skills = ArrayList<Skill>()
    private val switches = ArrayList<Switch>()
    private val tenants = ArrayList<Tenant>()
    private val transactions = ArrayList<Transaction>()

    @SuppressWarnings("ComplexMethod")
    fun add(cfgObject: ICfgObject) {
        val configurationObject = toConfigurationObject(cfgObject)

        configurationObject?.let {
            when (cfgObject) {
                is CfgAccessGroup -> accessGroups += it as AccessGroup
                is CfgActionCode -> actionCodes += it as ActionCode
                is CfgAgentGroup -> agentGroups += it as AgentGroup
                is CfgAppPrototype -> appPrototypes += it as AppPrototype
                is CfgDNGroup -> dnGroups += it as DNGroup
                is CfgDN -> dns += it as DN
                is CfgEnumerator -> enumerators += it as Enumerator
                is CfgEnumeratorValue -> enumeratorValues += it as EnumeratorValue
                is CfgFolder -> folders += it as Folder
                is CfgGVPCustomer -> gvpCustomers += it as GVPCustomer
                is CfgGVPIVRProfile -> gvpIVRProfiles += it as GVPIVRProfile
                is CfgGVPReseller -> gvpResellers += it as GVPReseller
                is CfgHost -> hosts += it as Host
                is CfgPerson -> persons += it as Person
                is CfgPlace -> places += it as Place
                is CfgPhysicalSwitch -> physicalSwitches += it as PhysicalSwitch
                is CfgRole -> roles += it as Role
                is CfgScript -> scripts += it as Script
                is CfgSkill -> skills += it as Skill
                is CfgSwitch -> switches += it as Switch
                is CfgTenant -> tenants += it as Tenant
                is CfgTransaction -> transactions += it as Transaction
                else -> Unit
            }
        }
    }

    fun build(metadata: Metadata) = Configuration(
        metadata,
        accessGroups.sorted(),
        actionCodes.sorted(),
        agentGroups.sorted(),
        appPrototypes.sorted(),
        dnGroups.sorted(),
        dns.sorted(),
        enumerators.sorted(),
        enumeratorValues.sorted(),
        folders.sorted(),
        gvpCustomers.sorted(),
        gvpIVRProfiles.sorted(),
        gvpResellers.sorted(),
        hosts.sorted(),
        persons.sorted(),
        places.sorted(),
        physicalSwitches.sorted(),
        roles.sorted(),
        scripts.sorted(),
        skills.sorted(),
        switches.sorted(),
        tenants.sorted(),
        transactions.sorted()
    )

    companion object {

        @SuppressWarnings("ComplexMethod")
        fun toConfigurationObject(cfgObject: ICfgObject): ConfigurationObject? =
            when (cfgObject) {
                is CfgAccessGroup -> AccessGroup(cfgObject)
                is CfgActionCode -> ActionCode(cfgObject)
                is CfgAgentGroup -> AgentGroup(cfgObject)
                is CfgAppPrototype -> AppPrototype(cfgObject)
                is CfgDNGroup -> DNGroup(cfgObject)
                is CfgDN -> DN(cfgObject)
                is CfgEnumerator -> Enumerator(cfgObject)
                is CfgEnumeratorValue -> EnumeratorValue(cfgObject)
                is CfgFolder -> Folder(cfgObject)
                is CfgGVPCustomer -> GVPCustomer(cfgObject)
                is CfgGVPIVRProfile -> GVPIVRProfile(cfgObject)
                is CfgGVPReseller -> GVPReseller(cfgObject)
                is CfgHost -> Host(cfgObject)
                is CfgPerson -> Person(cfgObject)
                is CfgPlace -> Place(cfgObject)
                is CfgPhysicalSwitch -> PhysicalSwitch(cfgObject)
                is CfgRole -> Role(cfgObject)
                is CfgScript -> Script(cfgObject)
                is CfgSkill -> Skill(cfgObject)
                is CfgSwitch -> Switch(cfgObject)
                is CfgTenant -> Tenant(cfgObject)
                is CfgTransaction -> Transaction(cfgObject)
                else -> null
            }
    }
}
