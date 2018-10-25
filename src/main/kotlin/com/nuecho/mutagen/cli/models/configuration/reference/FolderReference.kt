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

package com.nuecho.mutagen.cli.models.configuration.reference

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.objects.CfgOwnerID
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGCampaign
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGEnumerator
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGGVPCustomer
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGGVPReseller
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGIVR
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGSwitch
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGTenant
import com.nuecho.mutagen.cli.getPath
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectNotFoundException
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectType
import com.nuecho.mutagen.cli.services.getObjectDbid
import com.nuecho.mutagen.cli.services.retrieveObject
import com.nuecho.mutagen.cli.toShortName

data class FolderReference(val type: String, val owner: OwnerReference, val path: List<String> = emptyList()) :
    ConfigurationObjectReference<CfgFolder>(CfgFolder::class.java) {

    constructor(folder: CfgFolder) : this(folder.type.toShortName(), folder.ownerID.getReference(), folder.getPath())

    override fun toQuery(service: IConfService) = throw ConfigurationObjectNotFoundException(this)

    override fun compareTo(other: ConfigurationObjectReference<*>): Int {
        if (other !is FolderReference) return super.compareTo(other)

        return Comparator
            .comparing(FolderReference::type)
            .thenComparing(FolderReference::owner)
            .thenComparing { reference: FolderReference -> reference.path.toString() }
            .compare(this, other)
    }

    override fun toString() = "type: '$type', owner: '$owner', path: '${path.joinToString("/")}'"

    fun toFolderDbid(service: IConfService) = service.retrieveObject(this)?.objectDbid
            ?: throw ConfigurationObjectNotFoundException(this)

    @JsonIgnore
    @Suppress("DataClassContainsFunctions")
    fun isRoot() = path.isEmpty()
}

data class OwnerReference(val type: String, val name: String, val tenant: TenantReference? = null) :
    Comparable<OwnerReference> {

    override fun compareTo(other: OwnerReference): Int = Comparator
        .comparing { reference: OwnerReference -> reference.tenant ?: TenantReference("") }
        .thenComparing(OwnerReference::type)
        .thenComparing(OwnerReference::name)
        .compare(this, other)

    override fun toString() = if (tenant != null) "$type/$tenant/$name" else "$type/$name"

    private fun toConfigurationObjectReference() = when (toCfgObjectType(type)) {
        CFGCampaign -> CampaignReference(name, tenant)
        CFGEnumerator -> EnumeratorReference(name, tenant)
        CFGGVPCustomer -> GVPCustomerReference(name)
        CFGGVPReseller -> GVPResellerReference(name, tenant)
        CFGIVR -> IVRReference(name)
        CFGSwitch -> SwitchReference(name, tenant)
        CFGTenant -> TenantReference(name)
        else -> throw IllegalArgumentException("Illegal owner type: '$type'")
    }

    fun toCfgOwnerID(parent: CfgObject) = CfgOwnerID(parent.configurationService, parent).also {
        it.type = toCfgObjectType(type)
        it.dbid = parent.configurationService.getObjectDbid(toConfigurationObjectReference())
    }
}
