package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.nuecho.genesys.cli.models.configuration.Script
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportScriptTest : ImportObjectSpec(CfgScript(mockConfService()), listOf(Script("foo"), Script("bar")))
