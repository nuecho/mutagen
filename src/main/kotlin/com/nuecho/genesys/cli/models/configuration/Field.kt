package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgField
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgDataType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFieldType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FieldReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

data class Field(
    val tenant: TenantReference,
    val name: String,
    val defaultValue: String? = null,
    val description: String? = null,
    val fieldType: String? = null,
    @get:JsonProperty("isNullable")
    val isNullable: Boolean? = null,
    @get:JsonProperty("isPrimaryKey")
    val isPrimaryKey: Boolean? = null,
    @get:JsonProperty("isUnique")
    val isUnique: Boolean? = null,
    val length: Int? = null,
    val type: String? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject {

    @get:JsonIgnore
    override val reference = FieldReference(name, tenant)

    constructor(cfgField: CfgField) : this(
        tenant = cfgField.tenant.getReference(),
        name = cfgField.name,
        defaultValue = cfgField.defaultValue,
        description = cfgField.description,
        fieldType = cfgField.fieldType.toShortName(),
        isNullable = cfgField.isNullable.asBoolean(),
        isPrimaryKey = cfgField.isPrimaryKey.asBoolean(),
        isUnique = cfgField.isUnique.asBoolean(),
        length = cfgField.length,
        type = cfgField.type.toShortName(),
        state = cfgField.state?.toShortName(),
        userProperties = cfgField.userProperties?.asCategorizedProperties(),
        folder = cfgField.getFolderReference()
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgField(service))

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject): CfgField =
        (cfgObject as CfgField).also {
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("name", name, it)
            setProperty("description", description, it)
            setProperty("defaultValue", defaultValue, it)
            setProperty(FIELD_TYPE, toCfgFieldType(fieldType), it)
            setProperty("isNullable", toCfgFlag(isNullable), it)
            setProperty("isPrimaryKey", toCfgFlag(isPrimaryKey), it)
            setProperty("isUnique", toCfgFlag(isUnique), it)
            setProperty("length", length, it)
            setProperty("type", toCfgDataType(type), it)
            setProperty("state", ConfigurationObjects.toCfgObjectState(state), it)
            setProperty("userProperties", ConfigurationObjects.toKeyValueCollection(userProperties), it)
            setFolder(folder, it)
        }

    override fun cloneBare() = null

    override fun checkMandatoryProperties(service: ConfService): Set<String> {
        val missingMandatoryProperties = mutableSetOf<String>()
        fieldType ?: missingMandatoryProperties.add(FIELD_TYPE)
        return missingMandatoryProperties
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(folder)
            .add(tenant)
            .toSet()
}
