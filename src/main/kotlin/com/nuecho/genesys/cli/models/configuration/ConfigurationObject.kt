package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.services.ConfService

@SuppressWarnings("ComplexInterface")
interface ConfigurationObject : Comparable<ConfigurationObject> {
    val reference: ConfigurationObjectReference<*>
    val folder: FolderReference?
    val userProperties: CategorizedProperties?

    override fun compareTo(other: ConfigurationObject) = reference.compareTo(other.reference)

    fun createCfgObject(service: IConfService): CfgObject

    fun updateCfgObject(service: IConfService, cfgObject: ICfgObject): CfgObject

    fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String>

    /**
     * This method is used to check whether an unchangeable property is being set to a different value than the one
     * on the confServer.
     * For optional properties, check should only be performed when property is actually set on the confServer (if
     * remote is null, it can be set by update)
     */
    fun checkUnchangeableProperties(cfgObject: CfgObject): Set<String>

    fun applyDefaultValues() {}

    /**
     * This method is used to break dependency cycles when importing configuration objects.
     *
     * Should be overridden by cloneBare() = null if the ConfigurationObject has no nullable references to other objects
     * or only has non nullable tenant and folder (TenantReference and FolderReference)
     *
     * Returns a bare clone of this configuration object containing only:
     * - mandatory properties
     * - properties that, once set, cannot be modified
     * Don't forget to also check properties inside mandatory sub-objects.
     *
     * Note that you can safely optimize the cycle breaking process by following those rules:
     * - Ignore the `TenantReference` - cycles with tenants can always be broken at the tenant's level.
     * - Ignore the `folder` field - cycles with folders can always be broken at the Folder's level.
     * Those are just optimizations. When in doubt, just override this method with a non-null configuration object.
     */
    fun cloneBare(): ConfigurationObject?

    @JsonIgnore
    fun getReferences(): Set<ConfigurationObjectReference<*>>

    fun toJson() = defaultJsonObjectMapper()
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(this)!!
}
