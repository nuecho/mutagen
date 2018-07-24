@file:Suppress("MethodOverloading")

package com.nuecho.genesys.cli

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgAccessGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgCallingList
import com.genesyslab.platform.applicationblocks.com.objects.CfgCampaign
import com.genesyslab.platform.applicationblocks.com.objects.CfgCampaignGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumeratorValue
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPCustomer
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPIVRProfile
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPReseller
import com.genesyslab.platform.applicationblocks.com.objects.CfgIVR
import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgOwnerID
import com.genesyslab.platform.applicationblocks.com.objects.CfgParentID
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlaceGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.applicationblocks.com.objects.CfgStatTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.applicationblocks.com.objects.CfgTimeZone
import com.genesyslab.platform.commons.GEnum
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.configuration.protocol.types.CfgDNRegisterFlag
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgFolderClass
import com.genesyslab.platform.configuration.protocol.types.CfgIVRProfileType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGFolder
import com.genesyslab.platform.configuration.protocol.types.CfgTransactionType
import com.genesyslab.platform.reporting.protocol.statserver.AgentStatus
import com.genesyslab.platform.reporting.protocol.statserver.DnActions
import com.genesyslab.platform.reporting.protocol.statserver.DnActionsMask
import com.genesyslab.platform.reporting.protocol.statserver.DnStatus
import com.genesyslab.platform.reporting.protocol.statserver.DnStatusesCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.CFG_DN_REGISTER_FLAG_PREFIX
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.CFG_FOLDER_CLASS_PREFIX
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.CFG_IVR_PROFILE_PREFIX
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.CFG_PREFIX
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.CFG_TRANSACTION_PREFIX
import com.nuecho.genesys.cli.models.configuration.reference.AccessGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.AgentGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.AgentLoginReference
import com.nuecho.genesys.cli.models.configuration.reference.ApplicationReference
import com.nuecho.genesys.cli.models.configuration.reference.CallingListReference
import com.nuecho.genesys.cli.models.configuration.reference.CampaignGroupCampaignReference
import com.nuecho.genesys.cli.models.configuration.reference.CampaignGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.CampaignReference
import com.nuecho.genesys.cli.models.configuration.reference.DNGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.EnumeratorReference
import com.nuecho.genesys.cli.models.configuration.reference.EnumeratorValueReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.GVPCustomerReference
import com.nuecho.genesys.cli.models.configuration.reference.GVPIVRProfileReference
import com.nuecho.genesys.cli.models.configuration.reference.GVPResellerReference
import com.nuecho.genesys.cli.models.configuration.reference.IVRReference
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.OwnerReference
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.PhysicalSwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.PlaceGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.PlaceReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.SkillReference
import com.nuecho.genesys.cli.models.configuration.reference.StatTableReference
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.TimeZoneReference
import java.time.ZoneId
import java.util.TimeZone

fun AgentStatus.getStatusAsDnAction(): DnActions =
    GEnum.getValue(DnActions::class.java, this.status)!! as DnActions

fun AgentStatus.isLoggedOut(): Boolean =
    this.getStatusAsDnAction() == DnActions.LoggedOut

fun AgentStatus.toConsoleString(): String = """
        |Agent (${this.agentId}):
        |  Status: ${this.getStatusAsDnAction()}
        |  Place : ${this.place.placeId ?: "<Unknown>"}
        |  DNs   : ${this.place.dnStatuses.toConsoleString()}
    """.trimMargin()

fun CfgApplication.getDefaultEndpoint(): Endpoint? {
    val port = getDefaultPort() ?: return null
    val hostname = this.serverInfo?.host?.name ?: return null
    return Endpoint(hostname, port)
}

fun CfgApplication.getDefaultPort(): Int? =
    this.portInfos?.find { it.id == "default" }?.port?.toInt()

fun DnStatusesCollection.toList(): List<DnStatus> =
    (0 until this.count).map { this.getItem(it) }

fun DnStatusesCollection.toConsoleString(): String =
    this.toList().map { "DN: ${it.dnId} - Switch: ${it.switchId}" }.toString()

fun DnActionsMask.setBits(vararg dnActions: DnActions) =
    dnActions.forEach { setBit(it) }

fun CfgTimeZone.toTimeZoneId(): String =
    TimeZone.getTimeZone(ZoneId.of(this.name, ZoneId.SHORT_IDS)).getDisplayName(false, TimeZone.SHORT)

fun GEnum.toShortName(): String =
    name().replace(CFG_PREFIX, "").toLowerCase()

fun CfgDNRegisterFlag.toShortName(): String = name().replace(CFG_DN_REGISTER_FLAG_PREFIX, "").toLowerCase()
fun CfgFolderClass.toShortName(): String = name().replace(CFG_FOLDER_CLASS_PREFIX, "").toLowerCase()
fun CfgIVRProfileType.toShortName(): String = name().replace(CFG_IVR_PROFILE_PREFIX, "").toLowerCase()
fun CfgTransactionType.toShortName(): String = name().replace(CFG_TRANSACTION_PREFIX, "").toLowerCase()

fun CfgFlag.asBoolean() =
    when (this) {
        CfgFlag.CFGNoFlag -> null
        CfgFlag.CFGTrue -> true
        CfgFlag.CFGFalse -> false
        else -> throw IllegalArgumentException("Illegal CfgFlag value: '$this'")
    }

fun CfgObject.getFolderReference() =
    if (folderId == null || folderId == 0) null
    else (configurationService.retrieveObject(CFGFolder, folderId) as CfgFolder).getReference()

fun CfgFolder.getPath(): List<String> {
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
fun CfgOwnerID.getReference() =
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

fun CfgAccessGroup.getReference() = AccessGroupReference(groupInfo.name, groupInfo.tenant.getReference())
fun CfgAgentGroup.getReference() = AgentGroupReference(groupInfo.name, groupInfo.tenant.getReference())
fun CfgAgentLogin.getReference() = AgentLoginReference(loginCode, switch.getReference())
fun CfgApplication.getReference() = ApplicationReference(name)
fun CfgCallingList.getReference() = CallingListReference(name)
fun CfgCampaign.getReference() = CampaignReference(name, tenant.getReference())
fun CfgCampaignGroup.getReference() =
    CampaignGroupReference(CampaignGroupCampaignReference(campaign.name, campaign.tenant.getReference()), name)

fun CfgDN.getReference() = DNReference(this)
fun CfgDNGroup.getReference() = DNGroupReference(groupInfo.name, groupInfo.tenant.getReference())
fun CfgEnumerator.getReference() = EnumeratorReference(name, tenant.getReference())
fun CfgEnumeratorValue.getReference() = EnumeratorValueReference(name, enumerator.getReference())
fun CfgFolder.getReference() = FolderReference(this)
fun CfgGVPCustomer.getReference() = GVPCustomerReference(name)
fun CfgGVPIVRProfile.getReference() = GVPIVRProfileReference(name)
fun CfgGVPReseller.getReference() = GVPResellerReference(name, tenant.getReference())
fun CfgIVR.getReference() = IVRReference(name)
fun CfgObjectiveTable.getReference() = ObjectiveTableReference(name, tenant.getReference())
fun CfgPerson.getReference() = PersonReference(employeeID, tenant.getReference())
fun CfgPhysicalSwitch.getReference() = PhysicalSwitchReference(name)
fun CfgPlace.getReference() = PlaceReference(name, tenant.getReference())
fun CfgPlaceGroup.getReference() = PlaceGroupReference(groupInfo.name, groupInfo.tenant.getReference())
fun CfgScript.getReference() = ScriptReference(name, tenant.getReference())
fun CfgSkill.getReference() = SkillReference(name, tenant.getReference())
fun CfgStatTable.getReference() = StatTableReference(name, tenant.getReference())
fun CfgSwitch.getReference() = SwitchReference(name, tenant.getReference())
fun CfgTenant.getReference() = TenantReference(name)
fun CfgTimeZone.getReference() = TimeZoneReference(name, tenant.getReference())

// For some reason, those methods are missing from CfgParentID
private fun CfgParentID.getDBID() = getProperty("DBID") as Int

private fun CfgParentID.getType(): CfgObjectType =
    GEnum.getValue(CfgObjectType::class.java, getProperty("type") as Int) as CfgObjectType
