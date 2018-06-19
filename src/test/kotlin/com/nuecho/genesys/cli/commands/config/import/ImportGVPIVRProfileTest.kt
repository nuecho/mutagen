package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPIVRProfile
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.GVPIVRProfile
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportGVPIVRProfileTest : ImportObjectSpec(
    CfgGVPIVRProfile(mockConfService()),
    GVPIVRProfile(tenant = DEFAULT_TENANT_REFERENCE, name = "foo")
)
