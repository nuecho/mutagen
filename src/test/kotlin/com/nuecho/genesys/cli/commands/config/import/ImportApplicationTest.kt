package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.nuecho.genesys.cli.models.configuration.Application
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportApplicationTest : ImportObjectSpec(
    CfgApplication(mockConfService()),
    Application("foo")
)
