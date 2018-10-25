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
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumeratorValue
import com.nuecho.mutagen.cli.asBoolean
import com.nuecho.mutagen.cli.core.InitializingBean
import com.nuecho.mutagen.cli.getFolderReference
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.mutagen.cli.models.configuration.reference.EnumeratorReference
import com.nuecho.mutagen.cli.models.configuration.reference.EnumeratorValueReference
import com.nuecho.mutagen.cli.models.configuration.reference.FolderReference
import com.nuecho.mutagen.cli.models.configuration.reference.TenantReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.services.getObjectDbid
import com.nuecho.mutagen.cli.toShortName

data class EnumeratorValue(
    val enumerator: EnumeratorReference,
    val name: String,
    // isDefault will be true if not specified, but an exception is thrown if more than 1 enumeratorValue is default
    @get:JsonProperty("isDefault")
    val isDefault: Boolean = false,
    val description: String? = null,
    val displayName: String? = null,
    val state: String? = null,
    val tenant: TenantReference? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = EnumeratorValueReference(name, enumerator)

    constructor(enumeratorValue: CfgEnumeratorValue) : this(
        tenant = enumeratorValue.tenant.getReference(),
        isDefault = enumeratorValue.isDefault?.asBoolean()!!,
        description = enumeratorValue.description,
        displayName = enumeratorValue.displayName,
        enumerator = enumeratorValue.enumerator.let { enumerator ->
            EnumeratorReference(enumerator.name, TenantReference(enumeratorValue.tenant.name))
        },
        name = enumeratorValue.name,
        state = enumeratorValue.state.toShortName(),
        userProperties = enumeratorValue.userProperties?.asCategorizedProperties(),
        folder = enumeratorValue.getFolderReference()
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgEnumeratorValue(service).also {
            applyDefaultValues()
            setProperty("enumeratorDBID", service.getObjectDbid(enumerator), it)
            setProperty("name", name, it)
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setFolder(folder, it)
        })

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgEnumeratorValue).also { cfgEnumeratorValue ->
            setProperty("description", description, cfgEnumeratorValue)
            setProperty(DISPLAY_NAME, displayName, cfgEnumeratorValue)
            setProperty("isDefault", ConfigurationObjects.toCfgFlag(isDefault), cfgEnumeratorValue)
            setProperty("state", toCfgObjectState(state), cfgEnumeratorValue)
            setProperty("userProperties", ConfigurationObjects.toKeyValueCollection(userProperties), cfgEnumeratorValue)
        }

    override fun cloneBare() = null

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> {
        val missingMandatoryProperties = mutableSetOf<String>()
        displayName ?: missingMandatoryProperties.add(DISPLAY_NAME)
        tenant ?: missingMandatoryProperties.add(TENANT)

        return missingMandatoryProperties
    }

    // In theory, tenant is unchangeable, but changing the tenant changes the enumerator's reference and therefore
    // changes the enumeratorValue reference, creating a new EnumeratorValue.
    override fun checkUnchangeableProperties(cfgObject: CfgObject) = checkUnchangeableProperties(this, cfgObject)

    override fun afterPropertiesSet() {
        enumerator.tenant = tenant
    }

    override fun applyDefaultValues() {
//        default = false
//        displayName = name
//        state = "enabled"
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(enumerator)
            .add(tenant)
            .add(folder)
            .toSet()
}
