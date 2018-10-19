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
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAccessGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgID
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPerson
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgAccessGroupType
import com.nuecho.genesys.cli.models.configuration.reference.AccessGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

data class AccessGroup(
    val group: Group,
    val members: List<PersonReference>? = null,
    val type: String? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = AccessGroupReference(group.name, group.tenant)

    override val userProperties: CategorizedProperties?
        @JsonIgnore
        get() = group.userProperties

    constructor(accessGroup: CfgAccessGroup) : this(
        group = Group(accessGroup.groupInfo),
        members = accessGroup.memberIDs?.mapNotNull {
            accessGroup.configurationService.retrieveObject(CFGPerson, it.dbid) as CfgPerson
        }?.map { it.getReference() },
        type = accessGroup.type?.toShortName(),
        folder = accessGroup.getFolderReference()
    )

    constructor(tenant: TenantReference, name: String) : this(
        group = Group(tenant, name)
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgAccessGroup(service)).also {
            setProperty("type", toCfgAccessGroupType(type), it)
            setFolder(folder, it)
        }

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgAccessGroup).also {
            val groupInfo = group.toUpdatedCfgGroup(service, it.groupInfo ?: CfgGroup(service, it)).also { cfgGroup ->
                it.dbid?.let { dbid -> cfgGroup.dbid = dbid }
            }

            setProperty("groupInfo", groupInfo, it)
            setProperty("memberIDs", toCfgIDList(members, it), it)
        }

    override fun cloneBare() = AccessGroup(Group(group.tenant, group.name))

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> = emptySet()

    override fun checkUnchangeableProperties(cfgObject: CfgObject) =
        checkUnchangeableProperties(this, cfgObject).also { unchangeableProperties ->
            (cfgObject as CfgAccessGroup).also {
                if (type != null && it.type != null && type.toLowerCase() != it.type.toShortName())
                    unchangeableProperties.add(TYPE)
            }
        }

    override fun afterPropertiesSet() {
        group.updateTenantReferences()
        members?.forEach { it.tenant = group.tenant }
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(members)
            .add(group.getReferences())
            .add(folder)
            .toSet()
}

private fun toCfgIDList(members: List<PersonReference>?, parent: CfgObject) = members?.map {
    val service = parent.configurationService

    CfgID(service, parent).apply {
        dbid = service.getObjectDbid(it)
        type = CfgObjectType.CFGPerson
    }
}
