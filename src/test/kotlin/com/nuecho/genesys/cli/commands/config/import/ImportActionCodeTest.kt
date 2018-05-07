package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.nuecho.genesys.cli.models.configuration.ActionCode
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportActionCodeTest : ImportObjectSpec(
    CfgActionCode(mockConfService()),
    listOf(
        ActionCode(tenant = DEFAULT_TENANT_REFERENCE, name = "actionCode1"),
        ActionCode(tenant = DEFAULT_TENANT_REFERENCE, name = "actionCode2")
    )
)
