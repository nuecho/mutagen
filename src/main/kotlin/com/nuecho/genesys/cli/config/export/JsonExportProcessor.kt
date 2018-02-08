package com.nuecho.genesys.cli.config.export

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.nuecho.genesys.cli.config.ConfigurationObjectType
import java.io.OutputStream
import java.io.StringWriter
import java.util.SortedMap
import java.util.TreeMap
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class JsonExportProcessor(output: OutputStream) : ExportProcessor {
    val xmlMapper = XmlMapper()
    val xmlTransformer = TransformerFactory.newInstance().newTransformer()
    var jsonGenerator: JsonGenerator
    var configurationObjects: SortedMap<String, ICfgObject> = TreeMap()

    init {
        jsonGenerator = JsonFactory().createGenerator(output, JsonEncoding.UTF8)
        jsonGenerator.codec = ObjectMapper()
        jsonGenerator.prettyPrinter = DefaultPrettyPrinter()
    }

    override fun begin() {
        jsonGenerator.writeStartObject()
    }

    override fun beginType(type: ConfigurationObjectType) {
        configurationObjects.clear()
    }

    override fun processObject(type: ConfigurationObjectType, configurationObject: ICfgObject) {
        val objectId = type.getObjectId(configurationObject)
        configurationObjects[objectId] = configurationObject
    }

    override fun endType(type: ConfigurationObjectType) {
        jsonGenerator.writeArrayFieldStart(type.getObjectType())

        configurationObjects.values.forEach {
            val jsonNode = xmlMapper.readTree(toXml(it))
            jsonGenerator.writeObject(jsonNode)
        }

        jsonGenerator.writeEndArray()
    }

    override fun end() {
        jsonGenerator.writeEndObject()
        jsonGenerator.close()
    }

    fun toXml(configurationObject: ICfgObject): String {
        val writer = StringWriter()
        var node = configurationObject.toXml()
        xmlTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        xmlTransformer.transform(DOMSource(node), StreamResult(writer))
        writer.flush()
        return writer.toString()
    }
}
