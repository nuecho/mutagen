package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.nuecho.genesys.cli.models.configuration.Switch
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportSwitchTest : ImportObjectSpec(CfgSwitch(mockConfService()), listOf(Switch("foo"), Switch("bar")))
