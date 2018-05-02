package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.UNCHANGED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.services.getPersonDbid
import com.nuecho.genesys.cli.services.retrieveAgentGroup
import com.nuecho.genesys.cli.toPrimaryKeyList

data class AgentGroup(
    val agents: List<String>? = null,
    val group: Group
) : ConfigurationObject {
    override val primaryKey: String
        @JsonIgnore
        get() = group.primaryKey

    override val userProperties: CategorizedProperties?
        @JsonIgnore
        get() = group.userProperties

    constructor(agentGroup: CfgAgentGroup) : this(
        agents = agentGroup.agents?.toPrimaryKeyList(),
        group = Group(agentGroup.groupInfo)
    )

    constructor(name: String) : this(
        agents = emptyList(),
        group = Group(name)
    )

    override fun updateCfgObject(service: IConfService): ConfigurationObjectUpdateResult {
        service.retrieveAgentGroup(primaryKey)?.let {
            return ConfigurationObjectUpdateResult(UNCHANGED, it)
        }

        CfgAgentGroup(service).let {
            setProperty("agentDBIDs", agents?.map { service.getPersonDbid(it) }, it)
            setProperty("groupInfo", group.toCfgGroup(service, it), it)

            return ConfigurationObjectUpdateResult(ConfigurationObjectUpdateStatus.CREATED, it)
        }
    }
}
