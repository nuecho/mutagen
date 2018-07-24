package com.nuecho.genesys.cli.commands.config.import.operation

import com.nuecho.genesys.cli.commands.config.import.operation.ImportOperationType.CREATE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.services.ConfService

class CreateOperation(configurationObject: ConfigurationObject, service: ConfService) :
    ImportOperation(CREATE, configurationObject, service) {

    override fun apply() {
        save(configurationObject.createCfgObject(service))
    }
}
