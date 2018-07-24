package com.nuecho.genesys.cli.commands.config.import.operation

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.nuecho.genesys.cli.Console.ansiPrintln
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.toShortName

abstract class ImportOperation(
    val type: ImportOperationType,
    val configurationObject: ConfigurationObject,
    val service: ConfService
) {
    abstract fun apply()

    fun print(detailed: Boolean = true) {
        val reference = configurationObject.reference
        val buffer = StringBuffer()

        buffer.append("@|${typeToColor[type]} ")
        buffer.append("$type ${reference.getCfgObjectType().toShortName()} => $reference")

        if (detailed) {
            // TODO: move part of this within Console (i.e. print object to console with margin or something)
            val printableConfigurationObject = configurationObject.toJson().replace("\n", "\n$PRINT_MARGIN")
            buffer.append("\n$PRINT_MARGIN$printableConfigurationObject")
        }

        buffer.append("|@")

        ansiPrintln(buffer.toString())
    }

    companion object {
        internal fun save(cfgObject: CfgObject) = cfgObject.save()
    }
}
