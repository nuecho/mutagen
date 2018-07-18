package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.applicationblocks.com.objects.CfgSubcode
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgActionCodeType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.ActionCodeReference
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

data class ActionCode(
    val tenant: TenantReference,
    val name: String,
    val type: String,
    val code: String? = null,
    val subcodes: Map<String, String>? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = ActionCodeReference(name, type, tenant)

    constructor(actionCode: CfgActionCode) : this(
        tenant = actionCode.tenant.getReference(),
        name = actionCode.name,
        type = actionCode.type.toShortName(),
        code = actionCode.code,
        subcodes = actionCode.subcodes?.map { it.name to it.code }?.toMap(),
        state = actionCode.state?.toShortName(),
        folder = actionCode.getFolderReference(),
        userProperties = actionCode.userProperties.asCategorizedProperties()
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgActionCode(service))

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgActionCode).also {
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("name", name, it)
            setProperty("type", toCfgActionCodeType(type), it)
            setProperty(CODE, code, it)
            setProperty("subcodes", toCfgSubcodeList(subcodes, it), it)
            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            setFolder(folder, it)
        }

    override fun checkMandatoryProperties(service: ConfService): Set<String> =
        if (code == null) setOf(CODE) else emptySet()

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(tenant)
            .add(folder)
            .toSet()
}

private fun toCfgSubcodeList(subcodes: Map<String, String>?, actionCode: CfgActionCode) =
    subcodes?.map { (name, code) ->
        CfgSubcode(actionCode.configurationService, actionCode).apply {
            this.name = name
            this.code = code
        }
    }?.toList()
