package com.nuecho.genesys.cli.models.configuration

data class Configuration(
    val __metadata__: Metadata,
    val accessGroups: List<AccessGroup> = emptyList(),
    val actionCodes: List<ActionCode> = emptyList(),
    val agentGroups: List<AgentGroup> = emptyList(),
    val dnGroups: List<DNGroup> = emptyList(),
    val dns: List<DN> = emptyList(),
    val enumerators: List<Enumerator> = emptyList(),
    val gvpCustomers: List<GVPCustomer> = emptyList(),
    val gvpIVRProfiles: List<GVPIVRProfile> = emptyList(),
    val gvpResellers: List<GVPReseller> = emptyList(),
    val persons: List<Person> = emptyList(),
    val physicalSwitches: List<PhysicalSwitch> = emptyList(),
    val roles: List<Role> = emptyList(),
    val scripts: List<Script> = emptyList(),
    val skills: List<Skill> = emptyList(),
    val switches: List<Switch> = emptyList(),
    val tenants: List<Tenant> = emptyList(),
    val transactions: List<Transaction> = emptyList()
) {
    fun toList() = listOf(
        accessGroups,
        actionCodes,
        agentGroups,
        dnGroups,
        dns,
        enumerators,
        gvpCustomers,
        gvpIVRProfiles,
        gvpResellers,
        persons,
        physicalSwitches,
        roles,
        scripts,
        skills,
        switches,
        tenants,
        transactions
    ).flatMap { it }

    fun toMapByReference() = toList().map { it.reference to it }.toMap()
}
