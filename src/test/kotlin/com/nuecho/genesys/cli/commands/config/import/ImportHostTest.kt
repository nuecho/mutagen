package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgHost
import com.nuecho.genesys.cli.models.configuration.Host
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportHostTest : ImportObjectSpec(
    CfgHost(mockConfService()),
    Host(name = "host1")
)
