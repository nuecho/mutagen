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

package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAppPrototype
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgAppType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.reference.AppPrototypeReference
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.toShortName

data class AppPrototype(
    val name: String,
    val type: String? = null,
    val version: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    val options: CategorizedProperties? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = AppPrototypeReference(name)

    constructor(appPrototype: CfgAppPrototype) : this(
        name = appPrototype.name,
        type = appPrototype.type?.toShortName(),
        state = appPrototype.state?.toShortName(),
        version = appPrototype.version,
        options = appPrototype.options?.asCategorizedProperties(),
        userProperties = appPrototype.userProperties?.asCategorizedProperties(),
        folder = appPrototype.getFolderReference()
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgAppPrototype(service)).also {
            setProperty("name", name, it)
            setProperty(TYPE, toCfgAppType(type), it)
            setProperty(VERSION, version, it)
            setFolder(folder, it)
        }

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgAppPrototype).also {
            setProperty("options", ConfigurationObjects.toKeyValueCollection(options), it)
            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", ConfigurationObjects.toKeyValueCollection(userProperties), it)
        }

    override fun cloneBare() = null

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> {
        val missingMandatoryProperties = mutableSetOf<String>()
        type ?: missingMandatoryProperties.add(TYPE)
        version ?: missingMandatoryProperties.add(VERSION)

        return missingMandatoryProperties
    }

    override fun checkUnchangeableProperties(cfgObject: CfgObject) =
        checkUnchangeableProperties(this, cfgObject).also { unchangeableProperties ->
            (cfgObject as CfgAppPrototype).also {
                type?.run { if (this.toLowerCase() != it.type?.toShortName()) unchangeableProperties.add(TYPE) }
                version?.run { if (this != it.version) unchangeableProperties.add(VERSION) }
            }
        }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(folder)
            .toSet()
}
