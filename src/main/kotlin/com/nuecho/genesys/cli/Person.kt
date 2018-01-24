package com.nuecho.genesys.cli

data class Person(val firstName: String,
                  val lastName: String,
                  val isAgent: Boolean = false,
                  val phoneNumbers: List<String> = emptyList())