package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFolderClass
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectType
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.toShortName

data class Folder(
    val name: String,
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
    override val reference = FolderReference(folder.type, folder.owner, folder.path + name)

    constructor(cfgFolder: CfgFolder) : this(
        name = cfgFolder.name,
        description = cfgFolder.description,
        folderClass = cfgFolder.folderClass?.toShortName(),
        customType = cfgFolder.customType,
        state = cfgFolder.state?.toShortName(),
        userProperties = cfgFolder.userProperties?.asCategorizedProperties(),
        folder = cfgFolder.getFolderReference() ?: rootFolderReference(cfgFolder)
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgFolder(service).also {
            if (!folder.isRoot()) {
                setFolder(folder, it)
            }

            setProperty("name", name, it)
            setProperty("ownerID", folder.owner.toCfgOwnerID(it), it)
            setProperty("type", toCfgObjectType(folder.type), it)
            setProperty("folderClass", toCfgFolderClass(folderClass), it)
        })

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgFolder).also {
            setProperty("description", description, it)
            setProperty("customType", customType, it)
            setProperty("state", ConfigurationObjects.toCfgObjectState(state), it)
            setProperty("userProperties", ConfigurationObjects.toKeyValueCollection(userProperties), it)
        }

    override fun cloneBare() = Folder(name = name, folder = folder)

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        if (folder.isRoot()) emptySet() else setOf(folder)
}

private fun rootFolderReference(rootFolder: CfgFolder) =
    FolderReference(
        rootFolder.type.toShortName(),
        rootFolder.ownerID.getReference()
    )
