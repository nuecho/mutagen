package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.nuecho.genesys.cli.models.configuration.Tenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportTenantTest : ImportObjectSpec(
    CfgTenant(mockConfService()),
    Tenant("foo")
)
