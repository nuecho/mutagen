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
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.nuecho.mutagen.cli.core.InitializingBean
import com.nuecho.mutagen.cli.getFolderReference
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgLinkType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.reference.ApplicationReference
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.mutagen.cli.models.configuration.reference.FolderReference
import com.nuecho.mutagen.cli.models.configuration.reference.PhysicalSwitchReference
import com.nuecho.mutagen.cli.models.configuration.reference.SwitchReference
import com.nuecho.mutagen.cli.models.configuration.reference.TenantReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.services.getObjectDbid
import com.nuecho.mutagen.cli.toShortName

/**
 * Unused fields address and contactPersonDBID are not defined.
 */
data class Switch(
    val tenant: TenantReference,
    val name: String,
    val physicalSwitch: PhysicalSwitchReference? = null,

    @get:JsonProperty("tServer")
    val tServer: ApplicationReference? = null,

    val linkType: String? = null,
    val switchAccessCodes: List<SwitchAccessCode>? = null,
    val dnRange: String? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = SwitchReference(name, tenant)

    constructor(switch: CfgSwitch) : this(
        tenant = switch.tenant.getReference(),
        name = switch.name,
        physicalSwitch = switch.physSwitch?.getReference(),
        tServer = switch.tServer?.getReference(),
        linkType = switch.linkType?.toShortName(),
        switchAccessCodes = switch.switchAccessCodes?.map { SwitchAccessCode(it) },
        dnRange = switch.dnRange,
        state = switch.state?.toShortName(),
        userProperties = switch.userProperties?.asCategorizedProperties(),
        folder = switch.getFolderReference()
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgSwitch(service)).also {
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("name", name, it)
            setProperty("physSwitchDBID", service.getObjectDbid(physicalSwitch), it)
            setFolder(folder, it)
        }

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgSwitch).also { switch ->
            setProperty("TServerDBID", service.getObjectDbid(tServer), switch)
            setProperty("linkType", toCfgLinkType(linkType), switch)
            setProperty(
                "switchAccessCodes",
                switchAccessCodes?.map { accessCode -> accessCode.toCfgSwitchAccessCode(service, switch) },
                switch
            )
            setProperty("DNRange", dnRange, switch)
            setProperty("state", toCfgObjectState(state), switch)
            setProperty("userProperties", toKeyValueCollection(userProperties), switch)
        }

    override fun cloneBare() = Switch(
        tenant = tenant,
        name = name,
        physicalSwitch = physicalSwitch
    )

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> =
        if (physicalSwitch == null) setOf(PHYSICAL_SWITCH) else emptySet()

    override fun checkUnchangeableProperties(cfgObject: CfgObject) =
        checkUnchangeableProperties(this, cfgObject).also { unchangeableProperties ->
            (cfgObject as CfgSwitch).also {
                physicalSwitch?.run {
                    if (this != it.physSwitch?.getReference()) unchangeableProperties.add(PHYSICAL_SWITCH)
                }
            }
        }

    override fun afterPropertiesSet() {
        switchAccessCodes?.forEach { it.updateTenantReferences(tenant) }
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(tenant)
            .add(physicalSwitch)
            .add(tServer)
            .add(switchAccessCodes?.mapNotNull { it.switch })
            .add(folder)
            .toSet()
}
