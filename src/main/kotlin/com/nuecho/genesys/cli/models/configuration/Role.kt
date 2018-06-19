package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAccessGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgRole
import com.nuecho.genesys.cli.Logging.warn
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.RoleReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName
import java.util.SortedSet

data class Role(
    val tenant: TenantReference,
    val name: String,
    val description: String? = null,
    val state: String? = null,
    val members: SortedSet<String>? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null
) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = RoleReference(name)

    constructor(role: CfgRole) : this(
        tenant = TenantReference(role.tenant.name),
        name = role.name,
        description = role.description,
        state = role.state?.toShortName(),
        userProperties = role.userProperties?.asCategorizedProperties(),
        members = role.members?.map {
            val dbid = it.objectDBID
            val type = it.objectType

            val member = role.configurationService.retrieveObject(type, dbid)

            val key = when (member) {
                null -> {
                    warn { "Cannot find $type object with DBID $dbid referred by role '${role.name}'." }
                    it.objectDBID.toString()
                }
                is CfgPerson -> member.employeeID
                is CfgAccessGroup -> member.groupInfo.name
                else -> throw IllegalArgumentException("Unexpected member type $type referred by role '${role.name}'.")
            }

            "${type.toShortName()}/$key"
        }?.toSortedSet()
    )

    override fun updateCfgObject(service: IConfService) =
        (service.retrieveObject(reference) ?: CfgRole(service)).also {
            // members are not updated
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("name", name, it)
            setProperty("description", description, it)
            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
        }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> = setOf(tenant)
}
