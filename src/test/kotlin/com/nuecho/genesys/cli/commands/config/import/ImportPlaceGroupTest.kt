package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgPlaceGroup
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.Group
import com.nuecho.genesys.cli.models.configuration.PlaceGroup
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportPlaceGroupTest : ImportObjectSpec(
    CfgPlaceGroup(mockConfService()),
    PlaceGroup(group = Group(tenant = DEFAULT_TENANT_REFERENCE, name = "group1"))
)
