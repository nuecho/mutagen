package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.UNCHANGED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.reference.AgentGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject

data class AgentGroup(
    val agents: List<PersonReference>? = null,
    val group: Group
) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = AgentGroupReference(group.reference)

    override val userProperties: CategorizedProperties?
        @JsonIgnore
        get() = group.userProperties

    constructor(agentGroup: CfgAgentGroup) : this(
        agents = agentGroup.agents?.map { it.getReference() },
        group = Group(agentGroup.groupInfo)
    )

    constructor(tenant: TenantReference, name: String) : this(
        agents = emptyList(),
        group = Group(tenant, name)
    )

    override fun updateCfgObject(service: IConfService): ConfigurationObjectUpdateResult {
        service.retrieveObject(reference)?.let {
            return ConfigurationObjectUpdateResult(UNCHANGED, it)
        }

        CfgAgentGroup(service).let {
            setProperty("agentDBIDs", agents?.mapNotNull { service.getObjectDbid(it) }, it)
            setProperty("groupInfo", group.toCfgGroup(service, it), it)

            return ConfigurationObjectUpdateResult(ConfigurationObjectUpdateStatus.CREATED, it)
        }
    }
}
