package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.nuecho.genesys.cli.models.configuration.AgentGroup
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.Group
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportAgentGroupTest : ImportObjectSpec(
    CfgAgentGroup(mockConfService()),
    AgentGroup(group = Group(tenant = DEFAULT_TENANT_REFERENCE, name = "group1"))
)
