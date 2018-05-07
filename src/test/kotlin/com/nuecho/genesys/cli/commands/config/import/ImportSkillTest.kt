package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.Skill
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportSkillTest : ImportObjectSpec(
    CfgSkill(mockConfService()),
    listOf(
        Skill(tenant = DEFAULT_TENANT_REFERENCE, name = "foo"),
        Skill(tenant = DEFAULT_TENANT_REFERENCE, name = "bar")
    )
)
