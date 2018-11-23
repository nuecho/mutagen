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
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgGroup
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGDN
import com.nuecho.mutagen.cli.core.InitializingBean
import com.nuecho.mutagen.cli.getFolderReference
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgDNGroupType
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.mutagen.cli.models.configuration.reference.DNGroupReference
import com.nuecho.mutagen.cli.models.configuration.reference.DNReference
import com.nuecho.mutagen.cli.models.configuration.reference.FolderReference
import com.nuecho.mutagen.cli.models.configuration.reference.TenantReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.toShortName

data class DNGroup(
    val group: Group,
    val dns: List<DNInfo>? = null,
    val type: String? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {

    @get:JsonIgnore
    override val reference = DNGroupReference(group.name, group.tenant)

    override val userProperties: CategorizedProperties?
        @JsonIgnore
        get() = group.userProperties

    constructor(dnGroup: CfgDNGroup) : this(
        group = Group(dnGroup.groupInfo),
        dns = dnGroup.dNs?.map { cfgDNInfo ->
            val dn = dnGroup.configurationService.retrieveObject(CFGDN, cfgDNInfo.dndbid) as CfgDN
            DNInfo(dn.getReference(), cfgDNInfo.trunks)
        },
        type = dnGroup.type.toShortName(),
        folder = dnGroup.getFolderReference()
    )

    constructor(tenant: TenantReference, name: String) : this(
        group = Group(tenant, name)
    )

    override fun createCfgObject(service: ConfService) =
        updateCfgObject(service, CfgDNGroup(service)).also {
            setProperty(TYPE, toCfgDNGroupType(type), it)
            setFolder(folder, it, service)
        }

    override fun updateCfgObject(service: ConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgDNGroup).also {
            val groupInfo = group.toUpdatedCfgGroup(service, it.groupInfo ?: CfgGroup(service, it)).also { cfgGroup ->
                it.dbid?.let { dbid -> cfgGroup.dbid = dbid }
            }

            setProperty("groupInfo", groupInfo, it)
            setProperty(
                "DNs",
                dns?.map { dnInfo ->
                    val dn = service.retrieveObject(dnInfo.dn)!!
                    CfgDNInfo(service, it).also {
                        setProperty("DNDBID", dn.dbid, it)
                        setProperty("trunks", dnInfo.trunks, it)
                    }
                },
                it
            )
        }

    override fun cloneBare() = DNGroup(
        group = Group(group.tenant, group.name),
        type = type
    )

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> =
        if (type == null) setOf(TYPE) else emptySet()

    override fun checkUnchangeableProperties(cfgObject: CfgObject) =
        checkUnchangeableProperties(this, cfgObject).also { unchangeableProperties ->
            (cfgObject as CfgDNGroup).also {
                type?.run { if (this.toLowerCase() != it.type?.toShortName()) unchangeableProperties.add(TYPE) }
            }
        }

    override fun afterPropertiesSet() {
        group.updateTenantReferences()
        dns?.forEach { it.dn.updateTenantReferences(group.tenant) }
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(dns?.map { it.dn })
            .add(group.getReferences())
            .add(folder)
            .toSet()
}

data class DNInfo(
    val dn: DNReference,
    val trunks: Int? = null
)
