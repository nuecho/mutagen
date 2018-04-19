package com.nuecho.genesys.cli.models.configuration.reference

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
import com.nuecho.genesys.cli.getPath
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectNotFoundException
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectType
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName

data class FolderReference(val type: String, val owner: OwnerReference, val path: List<String>) :
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
