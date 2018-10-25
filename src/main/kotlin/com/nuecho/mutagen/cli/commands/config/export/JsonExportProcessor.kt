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

package com.nuecho.mutagen.cli.commands.config.export

import com.fasterxml.jackson.core.JsonGenerator
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.nuecho.mutagen.cli.Logging.debug
import com.nuecho.mutagen.cli.models.configuration.ConfigurationBuilder
import com.nuecho.mutagen.cli.models.configuration.Metadata

class JsonExportProcessor(val metadata: Metadata, val jsonGenerator: JsonGenerator) : ExportProcessor {
    private val configurationBuilder = ConfigurationBuilder()

    override fun begin() {}

    override fun beginType(type: CfgObjectType) {}

    override fun processObject(cfgObject: ICfgObject) {
        debug { "Processing $cfgObject" }
        configurationBuilder.add(cfgObject)
    }

    override fun endType(type: CfgObjectType) {}

    override fun end() {
        jsonGenerator.writeObject(configurationBuilder.build(metadata))
        jsonGenerator.close()
    }
}
