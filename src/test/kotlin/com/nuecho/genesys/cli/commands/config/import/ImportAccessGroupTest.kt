package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgAccessGroup
import com.nuecho.genesys.cli.models.configuration.AccessGroup
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.Group
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportAccessGroupTest : ImportObjectSpec(
    CfgAccessGroup(mockConfService()),
    AccessGroup(group = Group(tenant = DEFAULT_TENANT_REFERENCE, name = "group1"))
)
