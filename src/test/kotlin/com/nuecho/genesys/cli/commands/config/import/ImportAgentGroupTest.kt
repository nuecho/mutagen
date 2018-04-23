package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.nuecho.genesys.cli.models.configuration.AgentGroup
import com.nuecho.genesys.cli.models.configuration.Group
import com.nuecho.genesys.cli.services.ServiceMocks

class ImportAgentGroupTest : ImportObjectSpec(
    CfgAgentGroup(ServiceMocks.mockConfService()),
    listOf(AgentGroup(group = Group(name = "group1")), AgentGroup(group = Group(name = "group2")))
)
