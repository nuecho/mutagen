/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nuecho.genesys.cli.commands.config.import.operation

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.nuecho.genesys.cli.Console.ansiPrintln
import com.nuecho.genesys.cli.commands.config.PRINT_MARGIN
import com.nuecho.genesys.cli.models.configuration.ConfigurationObject
import com.nuecho.genesys.cli.services.ConfService

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
        buffer.append("$type ${reference.toConsoleString()}")

        if (detailed) {
            // TODO: move part of this within Console (i.e. print object to console with margin or something)
            val printableConfigurationObject = configurationObject.toJson().replace("\n", "\n$PRINT_MARGIN")
            buffer.append("${System.lineSeparator()}$PRINT_MARGIN$printableConfigurationObject")
        }

        buffer.append("|@")

        ansiPrintln(buffer.toString())
    }

    companion object {
        internal fun save(cfgObject: CfgObject) = cfgObject.save()
    }
}
