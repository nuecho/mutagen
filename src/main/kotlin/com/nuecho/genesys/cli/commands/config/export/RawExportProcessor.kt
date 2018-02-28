package com.nuecho.genesys.cli.commands.config.export

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.nuecho.genesys.cli.core.defaultGenerator
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects
import org.json.XML.toJSONObject
import java.io.OutputStream
import java.io.StringWriter
import java.util.SortedMap
import java.util.TreeMap
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

private const val XMLNS = "xmlns"

class RawExportProcessor(output: OutputStream) : ExportProcessor {
    private val xmlTransformer = TransformerFactory.newInstance().newTransformer()
    private val objectMapper = jacksonObjectMapper()
    private val jsonGenerator: JsonGenerator = defaultGenerator(output)
    private val configurationObjects: SortedMap<String, ICfgObject> = TreeMap()

    override fun begin() {
        jsonGenerator.writeStartObject()
    }

    override fun beginType(type: CfgObjectType) {
        configurationObjects.clear()
    }

    override fun processObject(cfgObject: ICfgObject) {
        val primaryKey = ConfigurationObjects.getPrimaryKey(cfgObject)
        configurationObjects[primaryKey] = cfgObject
    }

    override fun endType(type: CfgObjectType) {
        val typeName = type.name()
        jsonGenerator.writeArrayFieldStart(typeName)

        configurationObjects.values.forEach {
            val xml = toXml(it)

            val jsonObject = toJSONObject(xml).getJSONObject(typeName.replace("CFG", "Cfg"))!!
            jsonObject.remove(XMLNS)

            val jsonNode = objectMapper.readTree(jsonObject.toString(0))
            jsonGenerator.writeObject(jsonNode)
        }

        jsonGenerator.writeEndArray()
    }

    override fun end() {
        jsonGenerator.writeEndObject()
        jsonGenerator.close()
    }

    private fun toXml(configurationObject: ICfgObject): String {
        val writer = StringWriter()
        val node = configurationObject.toXml()
        xmlTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        xmlTransformer.transform(DOMSource(node), StreamResult(writer))
        writer.flush()
        return writer.toString()
    }
}
