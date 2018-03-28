package com.nuecho.genesys.cli.models.configuration

import java.util.Collections.emptySortedMap
import java.util.SortedMap

data class Configuration(
    val persons: SortedMap<String, Person> = emptySortedMap(),
    val skills: SortedMap<String, Skill> = emptySortedMap()
)
