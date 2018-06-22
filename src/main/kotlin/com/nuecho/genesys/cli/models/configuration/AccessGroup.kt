package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAccessGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgID
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPerson
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgAccessGroupType
import com.nuecho.genesys.cli.models.configuration.reference.AccessGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName

data class AccessGroup(
    val group: Group,
    val members: List<PersonReference>? = null,
    val type: String? = null
) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = AccessGroupReference(group.name, group.tenant)

    override val userProperties: CategorizedProperties?
        @JsonIgnore
        get() = group.userProperties

    constructor(accessGroup: CfgAccessGroup) : this(
        group = Group(accessGroup.groupInfo),
        members = accessGroup.memberIDs?.map {
            accessGroup.configurationService.retrieveObject(CFGPerson, it.dbid) as CfgPerson
        }?.map { it.getReference() },
        type = accessGroup.type?.toShortName()
    )

    constructor(tenant: TenantReference, name: String) : this(
        group = Group(tenant, name)
    )

    override fun updateCfgObject(service: IConfService) =
        (service.retrieveObject(reference) ?: CfgAccessGroup(service)).also { cfgAccessGroup ->
            setProperty(
                "memberIDs",
                members?.map { person ->
                    CfgID(service, cfgAccessGroup).also {
                        setProperty("DBID", service.getObjectDbid(person), it)
                        setProperty("type", CFGPerson, it)
                    }
                },
                cfgAccessGroup
            )
            setProperty("groupInfo", group.toCfgGroup(service, cfgAccessGroup), cfgAccessGroup)
            setProperty("type", toCfgAccessGroupType(type), cfgAccessGroup)
        }

    override fun afterPropertiesSet() {
        group.updateTenantReferences()
        members?.forEach { it.tenant = group.tenant }
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(members)
            .add(group.getReferences())
            .toSet()
}
