package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLoginInfo
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.reference.AgentLoginReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference

data class AgentLoginInfo(
    val agentLogin: AgentLoginReference,
    val wrapupTime: Int
) {
    constructor(agentLoginInfo: CfgAgentLoginInfo) : this(
        agentLogin = agentLoginInfo.agentLogin.getReference(),
        wrapupTime = agentLoginInfo.wrapupTime
    )

    @Suppress("DataClassContainsFunctions")
    fun updateTenantReferences(tenant: TenantReference) = agentLogin.updateTenantReferences(tenant)
}
