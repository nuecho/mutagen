package com.nuecho.genesys.cli.commands.config.import.operation

import com.nuecho.genesys.cli.commands.config.import.operation.ImportOperationType.SKIP
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.services.ConfService

class SkipOperation(
    configurationObject: ConfigurationObject,
    service: ConfService
) : ImportOperation(SKIP, configurationObject, service) {

    override fun apply() {
        // Do nothing
    }
}
