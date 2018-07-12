package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgAppPrototype
import com.nuecho.genesys.cli.models.configuration.AppPrototype
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportAppPrototypeTest : ImportObjectSpec(
    CfgAppPrototype(mockConfService()),
    AppPrototype("foo")
)
