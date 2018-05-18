package com.nuecho.genesys.cli.commands.config.export

import com.fasterxml.jackson.core.JsonGenerator
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.nuecho.genesys.cli.core.defaultJsonGenerator
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import com.nuecho.genesys.cli.models.configuration.Metadata
import org.json.JSONArray
import org.json.JSONObject
import org.json.XML.toJSONObject
import java.io.OutputStream
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

private const val XMLNS = "xmlns"
private const val VALUE_KEY = "value"
private const val METADATA_KEY = "__metadata__"

class RawExportProcessor(output: OutputStream, val metadata: Metadata) : ExportProcessor {
    private val xmlTransformer = TransformerFactory.newInstance().newTransformer()
    private val objectMapper = defaultJsonObjectMapper()
    private val jsonGenerator: JsonGenerator = defaultJsonGenerator(output)
    private val configurationObjects: MutableList<ICfgObject> = ArrayList()

    override fun begin() {
        jsonGenerator.writeStartObject()
        jsonGenerator.writeFieldName(METADATA_KEY)
        jsonGenerator.writeObject(metadata)
    }

    override fun beginType(type: CfgObjectType) {
        configurationObjects.clear()
    }

    override fun processObject(cfgObject: ICfgObject) {
        configurationObjects += cfgObject
    }

    override fun endType(type: CfgObjectType) {
        val typeName = type.name()
        jsonGenerator.writeArrayFieldStart(typeName)

        configurationObjects
            .sortedBy { it.objectDbid }
            .forEach { configurationObject ->
                val xml = toXml(configurationObject)

                val jsonObject = toJSONObject(xml).getJSONObject(typeName.replace("CFG", "Cfg"))!!
                jsonObject.remove(XMLNS)
                prettifyObject(jsonObject)

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

    private fun prettifyObject(json: JSONObject): Any? {
        if (json.length() == 1 && json.has(VALUE_KEY)) {
            return json.get(VALUE_KEY)
        }

        json.keys().forEach {
            when (json[it]) {
                is JSONObject -> json.put(it, prettifyObject(json.getJSONObject(it)))
                is JSONArray -> json.put(it, prettifyArray(json.getJSONArray(it)))
                else -> return@forEach
            }
        }

        return json
    }

    private fun prettifyArray(json: JSONArray): Any? =
        json.map {
            when (it) {
                is JSONObject -> prettifyObject(it)
                is JSONArray -> prettifyArray(it)
                else -> it
            }
        }
}
