package com.nuecho.genesys.cli.commands.config.import.operation

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.nuecho.genesys.cli.commands.config.import.operation.ImportOperationType.UPDATE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.services.ConfService

class UpdateOperation(
    configurationObject: ConfigurationObject,
    private val cfgRemoteObject: ICfgObject,
    service: ConfService
) : ImportOperation(UPDATE, configurationObject, service) {

    override fun apply() {
        save(configurationObject.updateCfgObject(service, cfgRemoteObject))
    }
}
