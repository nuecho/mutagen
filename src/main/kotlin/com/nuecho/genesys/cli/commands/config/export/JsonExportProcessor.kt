package com.nuecho.genesys.cli.commands.config.export

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.nuecho.genesys.cli.Logging.debug
import com.nuecho.genesys.cli.models.configuration.ConfigurationBuilder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects
import java.io.OutputStream

class JsonExportProcessor(output: OutputStream) : ExportProcessor {
    private val jsonGenerator: JsonGenerator = JsonFactory().createGenerator(output, JsonEncoding.UTF8)
    private val configurationBuilder = ConfigurationBuilder()

    init {
        jsonGenerator.codec = jacksonObjectMapper()
        jsonGenerator.prettyPrinter = DefaultPrettyPrinter()
    }

    override fun begin() {}

    override fun beginType(type: CfgObjectType) {}

    override fun processObject(cfgObject: ICfgObject) {
        debug { "Processing ${ConfigurationObjects.getPrimaryKey(cfgObject)}." }
        configurationBuilder.add(cfgObject)
    }

    override fun endType(type: CfgObjectType) {}

    override fun end() {
        jsonGenerator.writeObject(configurationBuilder.build())
        jsonGenerator.close()
    }
}
