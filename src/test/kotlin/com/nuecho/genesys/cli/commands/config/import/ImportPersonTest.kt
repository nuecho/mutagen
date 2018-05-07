package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.Person
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportPersonTest : ImportObjectSpec(
    CfgPerson(mockConfService()),
    listOf(
        Person(tenant = DEFAULT_TENANT_REFERENCE, employeeId = "employee1", userName = "username1"),
        Person(tenant = DEFAULT_TENANT_REFERENCE, employeeId = "employee2", userName = "username2")
    )
)
