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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.nuecho.mutagen.cli.getFolderReference
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgFolderClass
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectType
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.mutagen.cli.models.configuration.reference.FolderReference
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.toShortName

data class Folder(
    val name: String,
    val type: String,
    val description: String? = null,
    val folderClass: String? = null,
    val customType: Int? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference
) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = FolderReference(type, folder.owner, folder.path + name)

    constructor(cfgFolder: CfgFolder) : this(
        name = cfgFolder.name,
        type = cfgFolder.type.toShortName(),
        description = cfgFolder.description,
        folderClass = cfgFolder.folderClass?.toShortName(),
        customType = cfgFolder.customType,
        state = cfgFolder.state?.toShortName(),
        userProperties = cfgFolder.userProperties?.asCategorizedProperties(),
        folder = cfgFolder.getFolderReference() ?: rootFolderReference(cfgFolder)
    )

    override fun createCfgObject(service: ConfService) =
        updateCfgObject(service, CfgFolder(service).also {
            if (!folder.isRoot()) {
                setFolder(folder, it, service)
            }

            setProperty("name", name, it)
            setProperty("ownerID", folder.owner.toCfgOwnerID(it, service), it)
            setProperty("type", toCfgObjectType(type), it)
            setProperty("folderClass", toCfgFolderClass(folderClass), it)
        })

    override fun updateCfgObject(service: ConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgFolder).also {
            setProperty("description", description, it)
            setProperty("customType", customType, it)
            setProperty("state", ConfigurationObjects.toCfgObjectState(state), it)
            setProperty("userProperties", ConfigurationObjects.toKeyValueCollection(userProperties), it)
        }

    override fun cloneBare() = Folder(name = name, type = type, folder = folder)

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> = emptySet()

    override fun checkUnchangeableProperties(cfgObject: CfgObject): Set<String> {
        val unchangeableProperties = checkUnchangeableProperties(this, cfgObject)
        (cfgObject as CfgFolder).let { cfgFolder ->
            if (type.toLowerCase() != cfgFolder.type.toShortName()) unchangeableProperties.add(TYPE)
        }
        return unchangeableProperties
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        if (folder.isRoot()) setOf(folder.owner.toConfigurationObjectReference()) else setOf(folder)
}

private fun rootFolderReference(rootFolder: CfgFolder) =
    FolderReference(
        rootFolder.type.toShortName(),
        rootFolder.ownerID.getReference()
    )
