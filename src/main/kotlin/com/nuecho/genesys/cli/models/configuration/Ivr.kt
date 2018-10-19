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
import com.genesyslab.platform.applicationblocks.com.objects.CfgIVR
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgIVRType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.ApplicationReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.IVRReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

data class Ivr(
    val name: String,
    val description: String? = null,
    val ivrServer: ApplicationReference? = null, // the type of this application must be CfgIVRInterfaceServer
    val tenant: TenantReference? = null,
    val type: String? = null,
    val version: String? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = IVRReference(name)

    constructor(cfgIvr: CfgIVR) : this(
        name = cfgIvr.name,
        description = cfgIvr.description,
        ivrServer = cfgIvr.ivrServer?.getReference(),
        tenant = cfgIvr.tenant?.getReference(),
        type = cfgIvr.type?.toShortName(),
        version = cfgIvr.version,
        state = cfgIvr.state.toShortName(),
        userProperties = cfgIvr.userProperties?.asCategorizedProperties(),
        folder = cfgIvr.getFolderReference()
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgIVR(service)).also {
            setProperty("name", name, it)
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty(TYPE, toCfgIVRType(type), it)
            setFolder(folder, it)
        }

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgIVR).also { cfgIvr ->
            setProperty("description", description, cfgIvr)
            setProperty("version", version, cfgIvr)
            setProperty("IVRServerDBID", service.getObjectDbid(ivrServer), cfgIvr)
            setProperty("state", toCfgObjectState(state), cfgIvr)
            setProperty("userProperties", toKeyValueCollection(userProperties), cfgIvr)
        }

    override fun cloneBare() = Ivr(name = name, tenant = tenant, type = type, version = version)

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> {
        val missingMandatoryProperties = mutableSetOf<String>()
        tenant ?: missingMandatoryProperties.add(TENANT)
        type ?: missingMandatoryProperties.add(TYPE)
        version ?: missingMandatoryProperties.add(VERSION)
        return missingMandatoryProperties
    }

    override fun checkUnchangeableProperties(cfgObject: CfgObject) =
        checkUnchangeableProperties(this, cfgObject).also { unchangeableProperties ->
            (cfgObject as CfgIVR).also {
                tenant?.run { if (this != it.tenant?.getReference()) unchangeableProperties.add(TENANT) }
                type?.run { if (this.toLowerCase() != it.type?.toShortName()) unchangeableProperties.add(TYPE) }
            }
        }

    override fun getReferences() = referenceSetBuilder()
        .add(folder)
        .add(ivrServer)
        .add(tenant)
        .toSet()
}
