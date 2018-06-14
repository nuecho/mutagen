package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPCustomer
import com.nuecho.genesys.cli.models.configuration.GVPCustomer
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportGVPCustomerTest : ImportObjectSpec(
    CfgGVPCustomer(mockConfService()),
    GVPCustomer(name = "foo")
)
