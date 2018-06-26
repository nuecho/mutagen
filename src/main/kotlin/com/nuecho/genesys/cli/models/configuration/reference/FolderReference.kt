package com.nuecho.genesys.cli.models.configuration.reference

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgCampaign
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPCustomer
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPReseller
import com.genesyslab.platform.applicationblocks.com.objects.CfgIVR
import com.genesyslab.platform.applicationblocks.com.objects.CfgOwnerID
import com.genesyslab.platform.applicationblocks.com.objects.CfgParentID
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.applicationblocks.com.queries.CfgFolderQuery
import com.genesyslab.platform.commons.GEnum
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGFolder
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.toShortName

data class FolderReference(val type: String, val owner: OwnerReference, val path: List<String>) :
    ConfigurationObjectReference<CfgFolder>(CfgFolder::class.java) {

    constructor(folder: CfgFolder) : this(folder.type.toShortName(), folder.ownerID.getReference(), folder.getPath())

    override fun toQuery(service: IConfService) =
        throw UnsupportedOperationException(
            "${javaClass.name} cannot be properly converted to ${CfgFolderQuery::class.java.name}"
        )

    override fun compareTo(other: ConfigurationObjectReference<*>): Int {
        if (other !is FolderReference) return super.compareTo(other)

        return Comparator
            .comparing(FolderReference::type)
            .thenComparing(FolderReference::owner)
            .thenComparing { reference: FolderReference -> reference.path.toString() }
            .compare(this, other)
    }

    override fun toString() = "type: '$type', owner: '$owner', path: '${path.joinToString("/")}'"
}

data class OwnerReference(val type: String, val name: String, val tenant: TenantReference? = null) :
    Comparable<OwnerReference> {

    override fun compareTo(other: OwnerReference): Int = Comparator
        .comparing { reference: OwnerReference -> reference.tenant ?: TenantReference("") }
        .thenComparing(OwnerReference::type)
        .thenComparing(OwnerReference::name)
        .compare(this, other)

    override fun toString() = if (tenant != null) "$type/$tenant/$name" else "$type/$name"
}

private fun CfgFolder.getPath(): List<String> {
    val path = mutableListOf(name)

    var parent = parentID
    while (parent.getType() == CFGFolder) {
        val parentFolder = configurationService.retrieveObject(CFGFolder, parent.getDBID()) as CfgFolder
        path.add(parentFolder.name)
        parent = parentFolder.parentID
    }

    path.reverse()
    return path
}

@Suppress("ComplexMethod")
private fun CfgOwnerID.getReference() =
    configurationService.retrieveObject(type, dbid).let {
        when (it) {
            is CfgCampaign -> OwnerReference(type.toShortName(), it.name, it.tenant.getReference())
            is CfgEnumerator -> OwnerReference(type.toShortName(), it.name, it.tenant.getReference())
            is CfgGVPCustomer -> OwnerReference(type.toShortName(), it.name)
            is CfgGVPReseller -> OwnerReference(type.toShortName(), it.name)
            is CfgIVR -> OwnerReference(type.toShortName(), it.name)
            is CfgSwitch -> OwnerReference(type.toShortName(), it.name, it.tenant.getReference())
            is CfgTenant -> OwnerReference(type.toShortName(), it.name)
            else -> throw IllegalArgumentException("Illegal owner type: '$type'")
        }
    }

// For some reason, those methods are missing from CfgParentID
private fun CfgParentID.getDBID() = getProperty("DBID") as Int

private fun CfgParentID.getType(): CfgObjectType =
    GEnum.getValue(CfgObjectType::class.java, getProperty("type") as Int) as CfgObjectType
