package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference

interface ConfigurationObject : Comparable<ConfigurationObject> {
    val reference: ConfigurationObjectReference<*>
    val folder: FolderReference?
    val userProperties: CategorizedProperties?

    override fun compareTo(other: ConfigurationObject) = reference.compareTo(other.reference)

    fun updateCfgObject(service: IConfService): CfgObject

    fun checkMandatoryProperties(): Set<String> = emptySet()

    fun applyDefaultValues() {}

    @JsonIgnore
    fun getReferences(): Set<ConfigurationObjectReference<*>>
}
