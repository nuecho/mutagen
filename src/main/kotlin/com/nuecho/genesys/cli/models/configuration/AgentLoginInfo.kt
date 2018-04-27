package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLoginInfo
import com.nuecho.genesys.cli.getPrimaryKey

data class AgentLoginInfo(
    val agentLogin: String,
    val wrapupTime: Int
) {
    constructor(agentLoginInfo: CfgAgentLoginInfo) : this(
        agentLogin = agentLoginInfo.agentLogin.getPrimaryKey(),
        wrapupTime = agentLoginInfo.wrapupTime
    )
}
