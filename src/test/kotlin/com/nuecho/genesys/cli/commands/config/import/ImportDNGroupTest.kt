package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgDNGroup
import com.genesyslab.platform.configuration.protocol.types.CfgDNGroupType.CFGACDQueues
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.DNGroup
import com.nuecho.genesys.cli.models.configuration.Group
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportDNGroupTest : ImportObjectSpec(
    CfgDNGroup(mockConfService()),
    DNGroup(group = Group(tenant = DEFAULT_TENANT_REFERENCE, name = "group1"), type = CFGACDQueues.name())
)
