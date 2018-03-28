package com.nuecho.genesys.cli.models.configuration

import java.util.SortedSet

data class Configuration(
    val persons: SortedSet<Person> = sortedSetOf<Person>(),
    val skills: SortedSet<Skill> = sortedSetOf<Skill>()
)
