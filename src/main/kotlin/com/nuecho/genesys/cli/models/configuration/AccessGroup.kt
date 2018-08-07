package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAccessGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPerson
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgAccessGroupType
import com.nuecho.genesys.cli.models.configuration.reference.AccessGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.toShortName

data class AccessGroup(
    val group: Group,
    val members: List<PersonReference>? = null,
    val type: String? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = AccessGroupReference(group.name, group.tenant)

    override val userProperties: CategorizedProperties?
        @JsonIgnore
        get() = group.userProperties

    constructor(accessGroup: CfgAccessGroup) : this(
        group = Group(accessGroup.groupInfo),
        members = accessGroup.memberIDs?.mapNotNull {
            accessGroup.configurationService.retrieveObject(CFGPerson, it.dbid) as CfgPerson
        }?.map { it.getReference() },
        type = accessGroup.type?.toShortName(),
        folder = accessGroup.getFolderReference()
    )

    constructor(tenant: TenantReference, name: String) : this(
        group = Group(tenant, name)
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgAccessGroup(service)).also {
            setProperty("type", toCfgAccessGroupType(type), it)
        }

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgAccessGroup).also { cfgAccessGroup ->
            val groupInfo = group.toCfgGroup(service, cfgAccessGroup).also {
                cfgAccessGroup.dbid?.let { dbid ->
                    it.dbid = dbid
                }
            }

            setProperty("groupInfo", groupInfo, cfgAccessGroup)
            setProperty("memberIDs", members?.map { it.toCfgID(service, cfgAccessGroup) }, cfgAccessGroup)
            setFolder(folder, cfgAccessGroup)
        }

    override fun cloneBare() = AccessGroup(Group(group.tenant, group.name))

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> = emptySet()

    override fun checkUnchangeableProperties(cfgObject: CfgObject): Set<String> {
        (cfgObject as CfgAccessGroup).also {
            if (type != null && it.type != null && type.toLowerCase() != it.type.toShortName())
                return setOf(TYPE)
        }

        return emptySet()
    }

    override fun afterPropertiesSet() {
        group.updateTenantReferences()
        members?.forEach { it.tenant = group.tenant }
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(members)
            .add(group.getReferences())
            .add(folder)
            .toSet()
}
