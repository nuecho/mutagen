package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgRole
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.Role
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportRoleTest : ImportObjectSpec(
    CfgRole(mockConfService()),
    Role(tenant = DEFAULT_TENANT_REFERENCE, name = "role1")
)
