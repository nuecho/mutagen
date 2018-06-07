package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction
import com.genesyslab.platform.configuration.protocol.types.CfgTransactionType.CFGTRTList
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.Transaction
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName

class ImportTransactionTest : ImportObjectSpec(
    CfgTransaction(mockConfService()),
    Transaction(tenant = DEFAULT_TENANT_REFERENCE, name = "foo", type = CFGTRTList.toShortName())
)
