package com.nuecho.genesys.cli.commands.config.import.operation

import com.nuecho.genesys.cli.commands.config.import.operation.ImportOperationType.UPDATE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.retrieveObject

class UpdateReferenceOperation(
    configurationObject: ConfigurationObject,
    service: ConfService
) : ImportOperation(UPDATE, configurationObject, service) {

    override fun apply() {
        val remoteCfgObject = service.retrieveObject(configurationObject.reference)!!
        save(configurationObject.updateCfgObject(service, remoteCfgObject))
    }
}
