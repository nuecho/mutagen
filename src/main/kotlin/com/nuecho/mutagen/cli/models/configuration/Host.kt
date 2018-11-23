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
import com.genesyslab.platform.applicationblocks.com.objects.CfgHost
import com.genesyslab.platform.applicationblocks.com.objects.CfgOS
import com.nuecho.mutagen.cli.getFolderReference
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgHostType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.reference.ApplicationReference
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.mutagen.cli.models.configuration.reference.FolderReference
import com.nuecho.mutagen.cli.models.configuration.reference.HostReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.toShortName

/**
 * Not in use HWID, address, contactPersonDBID and comment properties are not defined.
 */
data class Host(
    val name: String,
    val ipAddress: String? = null,
    val lcaPort: String? = null,
    val osInfo: OS? = null,
    val scs: ApplicationReference? = null,
    val type: String? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = HostReference(name)

    // FIXME ignoring resources property

    constructor(host: CfgHost) : this(
        name = host.name,
        ipAddress = host.iPaddress,
        lcaPort = host.lcaPort,
        osInfo = OS(host.oSinfo),
        scs = host.scs?.getReference(),
        type = host.type.toShortName(),
        state = host.state.toShortName(),
        userProperties = host.userProperties?.asCategorizedProperties(),
        folder = host.getFolderReference()
    )

    override fun createCfgObject(service: ConfService) =
        updateCfgObject(service, CfgHost(service)).also {
            setProperty("name", name, it)
            setProperty(TYPE, toCfgHostType(type), it)
            setFolder(folder, it, service)
        }

    override fun updateCfgObject(service: ConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgHost).also { cfgHost ->
            setProperty("IPaddress", ipAddress, cfgHost)
            setProperty("LCAPort", lcaPort, cfgHost)
            setProperty(
                "OSinfo",
                osInfo?.toUpdatedCfgOS(cfgHost.oSinfo ?: CfgOS(service, cfgHost)),
                cfgHost
            )
            setProperty("SCSDBID", service.getObjectDbid(scs), cfgHost)

            setProperty("userProperties", toKeyValueCollection(userProperties), cfgHost)
            setProperty("state", toCfgObjectState(state), cfgHost)
        }

    override fun cloneBare() = Host(
        name = name,
        lcaPort = lcaPort,
        osInfo = osInfo,
        type = type
    )

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> {
        val missingMandatoryProperties = mutableSetOf<String>()
        lcaPort ?: missingMandatoryProperties.add("lcaPort")
        osInfo ?: missingMandatoryProperties.add("osInfo")
        type ?: missingMandatoryProperties.add(TYPE)

        return missingMandatoryProperties
    }

    override fun checkUnchangeableProperties(cfgObject: CfgObject) =
        checkUnchangeableProperties(this, cfgObject).also { unchangeableProperties ->
            (cfgObject as CfgHost).also {
                type?.run { if (this.toLowerCase() != it.type?.toShortName()) unchangeableProperties.add(TYPE) }
            }
        }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(folder)
            .add(scs)
            .toSet()
}
