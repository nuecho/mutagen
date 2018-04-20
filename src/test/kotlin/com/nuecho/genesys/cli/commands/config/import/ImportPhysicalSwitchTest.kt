package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.nuecho.genesys.cli.models.configuration.PhysicalSwitch
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportPhysicalSwitchTest : ImportObjectSpec(CfgPhysicalSwitch(mockConfService()), listOf(PhysicalSwitch("foo"), PhysicalSwitch("bar")))
