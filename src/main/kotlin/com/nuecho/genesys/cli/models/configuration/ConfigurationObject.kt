package com.nuecho.genesys.cli.models.configuration

interface ConfigurationObject : Comparable<ConfigurationObject> {
    val primaryKey: String

    override fun compareTo(other: ConfigurationObject) = primaryKey.compareTo(other.primaryKey)
}
