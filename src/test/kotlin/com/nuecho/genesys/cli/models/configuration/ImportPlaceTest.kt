package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.nuecho.genesys.cli.commands.config.import.ImportObjectSpec
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportPlaceTest : ImportObjectSpec(
    CfgPlace(mockConfService()),
    Place(tenant = DEFAULT_TENANT_REFERENCE, name = "place")
)
