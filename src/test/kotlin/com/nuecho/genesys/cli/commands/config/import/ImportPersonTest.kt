package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.Person
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportPersonTest : ImportObjectSpec(
    CfgPerson(mockConfService()),
    Person(tenant = DEFAULT_TENANT_REFERENCE, employeeId = "employee1", userName = "username1")
)
