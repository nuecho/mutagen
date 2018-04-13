package com.nuecho.genesys.cli.models.configuration

data class Configuration(
    val actionCodes: Map<String, ActionCode> = emptyMap(),
    val enumerators: Map<String, Enumerator> = emptyMap(),
    val persons: Map<String, Person> = emptyMap(),
    val roles: Map<String, Role> = emptyMap(),
    val scripts: Map<String, Script> = emptyMap(),
    val skills: Map<String, Skill> = emptyMap(),
    val tenants: Map<String, Tenant> = emptyMap()
)
