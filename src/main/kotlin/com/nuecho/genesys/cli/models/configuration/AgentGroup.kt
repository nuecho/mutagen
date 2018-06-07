package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.reference.AgentGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject

data class AgentGroup(
    val agents: List<PersonReference>? = null,
    val group: Group
) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = AgentGroupReference(group.name, group.tenant)

    override val userProperties: CategorizedProperties?
        @JsonIgnore
        get() = group.userProperties

    constructor(agentGroup: CfgAgentGroup) : this(
        agents = agentGroup.agents?.map { it.getReference() },
        group = Group(agentGroup.groupInfo)
    )

    constructor(tenant: TenantReference, name: String) : this(
        group = Group(tenant, name)
    )

    override fun updateCfgObject(service: IConfService): CfgAgentGroup =
        (service.retrieveObject(reference) ?: CfgAgentGroup(service)).also {
            setProperty("agentDBIDs", agents?.mapNotNull { service.getObjectDbid(it) }, it)
            setProperty("groupInfo", group.toCfgGroup(service, it), it)
        }

    override fun afterPropertiesSet() = group.updateTenantReferences()

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(agents)
            .add(group.getReferences())
            .toSet()
}
