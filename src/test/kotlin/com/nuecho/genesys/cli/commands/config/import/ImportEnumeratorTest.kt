package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.Enumerator
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportEnumeratorTest : ImportObjectSpec(
    CfgEnumerator(mockConfService()),
    listOf(
        Enumerator(tenant = DEFAULT_TENANT_REFERENCE, name = "enumerator1"),
        Enumerator(tenant = DEFAULT_TENANT_REFERENCE, name = "enumerator2")
    )
)
