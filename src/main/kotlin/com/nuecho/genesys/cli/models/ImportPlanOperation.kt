package com.nuecho.genesys.cli.models

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.nuecho.genesys.cli.Logging
import com.nuecho.genesys.cli.models.ImportOperationType.CREATE
import com.nuecho.genesys.cli.models.ImportOperationType.UPDATE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName
import org.fusesource.jansi.Ansi

class ImportPlanOperation(val service: ConfService, val configurationObject: ConfigurationObject) {

    private val cfgRemoteObject: ICfgObject? = service.retrieveObject(configurationObject.reference)
    private val configurationObjectBackup: String? = cfgRemoteObject?.toString()

    var cfgObject: ICfgObject? = null

    val type: ImportOperationType =
        if (cfgRemoteObject == null) CREATE
        else UPDATE

    fun apply(): Boolean {

        val objectType = configurationObject.reference.getCfgObjectType().toShortName()
        Logging.info { "Processing $objectType '${configurationObject.reference}'." }

        cfgObject = if (type == CREATE)
            configurationObject.createCfgObject(service)
        else configurationObject.updateCfgObject(service, cfgRemoteObject!!)

        save(cfgObject as CfgObject)

        printStatus()

        // FIXME return something else
        return true
    }

    fun printStatus() {
        configurationObject.let {
            // XXX move part of this within Console
            println(
                Ansi.ansi().render(
                    "@|${typeToColor.get(type)} $type " +
                            "${it.reference.getCfgObjectType().toShortName()} " +
                            "=> ${it.reference}|@"
                )
            )
        }
    }

    fun print() {

        // XXX move part of this within Console (i.e. print object to console with margin or something
        val printableConfigurationObject = configurationObject
            .toJson().replace("\n", "\n$PRINT_MARGIN")

        // XXX move part of this within Console
        println(
            Ansi.ansi().render(
                "@|${typeToColor.get(type)} $type " +
                        "${configurationObject.reference.getCfgObjectType().toShortName()}" +
                        " => ${configurationObject.reference}\n  " +
                        "$printableConfigurationObject|@"
            )
        )
    }

    companion object {
        internal fun save(cfgObject: CfgObject) = cfgObject.save()
    }
}
