package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.nuecho.genesys.cli.models.configuration.Person
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportPersonTest : ImportObjectSpec(
    CfgPerson(mockConfService()),
    listOf(Person("employee1", "username1"), Person("employee2", "username2"))
)
