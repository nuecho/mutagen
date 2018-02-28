package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLoginInfo

data class AgentLoginInfo(
    val agentLogin: String,
    val wrapupTime: Int
) {
    constructor(agentLoginInfo: CfgAgentLoginInfo) : this(
        agentLogin = ConfigurationObjects.getPrimaryKey(agentLoginInfo.agentLogin)!!,
        wrapupTime = agentLoginInfo.wrapupTime
    )
}
