package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgAlarmCondition
import com.nuecho.genesys.cli.models.configuration.AlarmCondition
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportAlarmConditionTest : ImportObjectSpec(
    CfgAlarmCondition(mockConfService()),
    AlarmCondition(name = "host1")
)
