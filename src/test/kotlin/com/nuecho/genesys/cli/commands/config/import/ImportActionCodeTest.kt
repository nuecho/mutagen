package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.configuration.protocol.types.CfgActionCodeType.CFGTransfer
import com.nuecho.genesys.cli.models.configuration.ActionCode
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName

class ImportActionCodeTest : ImportObjectSpec(
    CfgActionCode(mockConfService()),
    listOf(
        ActionCode(tenant = DEFAULT_TENANT_REFERENCE, name = "actionCode1", type = CFGTransfer.toShortName()),
        ActionCode(tenant = DEFAULT_TENANT_REFERENCE, name = "actionCode2", type = CFGTransfer.toShortName())
    )
)
