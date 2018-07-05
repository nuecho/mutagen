package com.nuecho.genesys.cli.models

import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName
import org.fusesource.jansi.Ansi

class ImportPlanOperation(val service: ConfService, val configurationObject: ConfigurationObject) {
    var cfgObject = service.retrieveObject(configurationObject.reference)
    val type: ImportOperationType = if (cfgObject == null) ImportOperationType.CREATE else ImportOperationType.UPDATE

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
        val printableConfigurationObject = defaultJsonObjectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(configurationObject)
            .replace("\n", "\n$PRINT_MARGIN")

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
}
