package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.nuecho.genesys.cli.Logging.warn
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgScriptType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

data class Script(
    val tenant: TenantReference,
    val name: String,
    val type: String? = null,
    val index: Int? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = ScriptReference(name, tenant)

    constructor(script: CfgScript) : this(
        tenant = script.tenant.getReference(),
        name = script.name,
        type = script.type?.toShortName(),
        index = script.index,
        state = script.state?.toShortName(),
        userProperties = script.userProperties?.asCategorizedProperties(),
        folder = script.getFolderReference()
    ) {
        script.resources?.let { warn { "Unsupported ResourceObject collection in script object. Ignoring." } }
    }

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgScript(service).also { applyDefaultValues() })

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgScript).also {

            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("name", name, it)
            setProperty(TYPE, toCfgScriptType(type), it)
            setProperty("index", index, it)
            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            setFolder(folder, it)
        }

    override fun cloneBare() = null

    override fun checkMandatoryProperties(service: ConfService): Set<String> =
        if (type == null) setOf(TYPE) else emptySet()

    override fun applyDefaultValues() {
        // type = CfgScriptType.CFGNoScript.toShortName()
        // index = 0
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(tenant)
            .add(folder)
            .toSet()
}
