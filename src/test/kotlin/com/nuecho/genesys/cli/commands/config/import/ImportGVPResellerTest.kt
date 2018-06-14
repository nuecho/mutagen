package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPReseller
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.GVPReseller
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportGVPResellerTest : ImportObjectSpec(
    CfgGVPReseller(mockConfService()),
    GVPReseller(tenant = DEFAULT_TENANT_REFERENCE, name = "foo")
)
