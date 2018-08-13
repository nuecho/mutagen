package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.reference.AgentGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.getObjectDbid

data class AgentGroup(
    val agents: List<PersonReference>? = null,
    val group: Group,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = AgentGroupReference(group.name, group.tenant)

    override val userProperties: CategorizedProperties?
        @JsonIgnore
        get() = group.userProperties

    constructor(agentGroup: CfgAgentGroup) : this(
        agents = agentGroup.agents?.map { it.getReference() },
        group = Group(agentGroup.groupInfo),
        folder = agentGroup.getFolderReference()
    )

    constructor(tenant: TenantReference, name: String) : this(
        group = Group(tenant, name)
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgAgentGroup(service).also {
            setFolder(folder, it)
        })

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgAgentGroup).also { cfgAgentGroup ->
            val groupInfo = group.toCfgGroup(service, cfgAgentGroup).also {
                cfgAgentGroup.dbid?.let { dbid ->
                    it.dbid = dbid
                }
            }

            setProperty("groupInfo", groupInfo, cfgAgentGroup)
            setProperty("agentDBIDs", agents?.mapNotNull { service.getObjectDbid(it) }, cfgAgentGroup)
        }

    override fun cloneBare() = AgentGroup(group = Group(group.tenant, group.name))

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> = emptySet()

    override fun checkUnchangeableProperties(cfgObject: CfgObject) = checkUnchangeableProperties(this, cfgObject)

    override fun afterPropertiesSet() = group.updateTenantReferences()

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(agents)
            .add(group.getReferences())
            .add(folder)
            .toSet()
}
