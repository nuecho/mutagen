package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.IConfService

interface ConfigurationObject : Comparable<ConfigurationObject> {
    val primaryKey: String

    override fun compareTo(other: ConfigurationObject) = primaryKey.compareTo(other.primaryKey)

    fun updateCfgObject(service: IConfService): ConfigurationObjectUpdateResult
}
