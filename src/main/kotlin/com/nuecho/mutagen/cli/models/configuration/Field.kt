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

package com.nuecho.mutagen.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgField
import com.nuecho.mutagen.cli.asBoolean
import com.nuecho.mutagen.cli.getFolderReference
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgDataType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgFieldType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.mutagen.cli.models.configuration.reference.FieldReference
import com.nuecho.mutagen.cli.models.configuration.reference.FolderReference
import com.nuecho.mutagen.cli.models.configuration.reference.TenantReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.toShortName

data class Field(
    val tenant: TenantReference,
    val name: String,
    val defaultValue: String? = null,
    val description: String? = null,
    val fieldType: String? = null,
    @get:JsonProperty(IS_NULLABLE)
    val isNullable: Boolean? = null,
    @get:JsonProperty(IS_PRIMARY_KEY)
    val isPrimaryKey: Boolean? = null,
    @get:JsonProperty(IS_UNIQUE)
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
        name = cfgField.name,
        tenant = cfgField.tenant.getReference(),
        defaultValue = cfgField.defaultValue,
        description = cfgField.description,
        fieldType = cfgField.fieldType.toShortName(),
        isNullable = cfgField.isNullable?.asBoolean(),
        isPrimaryKey = cfgField.isPrimaryKey?.asBoolean(),
        isUnique = cfgField.isUnique?.asBoolean(),
        length = cfgField.length,
        type = cfgField.type.toShortName(),
        state = cfgField.state?.toShortName(),
        userProperties = cfgField.userProperties?.asCategorizedProperties(),
        folder = cfgField.getFolderReference()
    )

    override fun createCfgObject(service: ConfService) =
        updateCfgObject(service, CfgField(service)).also {
            setProperty("name", name, it)
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty(FIELD_TYPE, toCfgFieldType(fieldType), it)
            setProperty(IS_NULLABLE, toCfgFlag(isNullable), it)
            setProperty(IS_PRIMARY_KEY, toCfgFlag(isPrimaryKey), it)
            setProperty(IS_UNIQUE, toCfgFlag(isUnique), it)
            setProperty(TYPE, toCfgDataType(type), it)
            setFolder(folder, it, service)
        }

    override fun updateCfgObject(service: ConfService, cfgObject: ICfgObject): CfgField =
        (cfgObject as CfgField).also {
            setProperty("description", description, it)
            setProperty("defaultValue", defaultValue, it)
            if (!it.isSaved || it.length == 0) setProperty(LENGTH, length ?: 0, it)
            setProperty("state", ConfigurationObjects.toCfgObjectState(state), it)
            setProperty("userProperties", ConfigurationObjects.toKeyValueCollection(userProperties), it)
        }

    override fun cloneBare() = Field(
        tenant = tenant,
        name = name,
        fieldType = fieldType,
        isPrimaryKey = isPrimaryKey,
        isNullable = isNullable,
        isUnique = isUnique,
        length = length,
        type = type
    )

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> {
        val missingMandatoryProperties = mutableSetOf<String>()
        fieldType ?: missingMandatoryProperties.add(FIELD_TYPE)
        isNullable ?: missingMandatoryProperties.add(IS_NULLABLE)
        isPrimaryKey ?: missingMandatoryProperties.add(IS_PRIMARY_KEY)
        isUnique ?: missingMandatoryProperties.add(IS_UNIQUE)
        type ?: missingMandatoryProperties.add(TYPE)

        return missingMandatoryProperties
    }

    @Suppress("ComplexMethod")
    override fun checkUnchangeableProperties(cfgObject: CfgObject) =
        checkUnchangeableProperties(this, cfgObject).also { unchangeableProperties ->
            (cfgObject as CfgField).also {
                if (isNullable != null && isNullable != it.isNullable?.asBoolean())
                    unchangeableProperties.add(IS_NULLABLE)
                if (isPrimaryKey != null && isPrimaryKey != it.isPrimaryKey?.asBoolean())
                    unchangeableProperties.add(IS_PRIMARY_KEY)
                if (isUnique != null && isUnique != it.isUnique?.asBoolean())
                    unchangeableProperties.add(IS_UNIQUE)
                // as long as length is 0, it is not considered specified
                if (it.length != 0 && length != null && length != it.length)
                    unchangeableProperties.add(LENGTH)
                if (type != null && type.toLowerCase() != it.type?.toShortName())
                    unchangeableProperties.add(TYPE)
            }
        }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(folder)
            .add(tenant)
            .toSet()
}
