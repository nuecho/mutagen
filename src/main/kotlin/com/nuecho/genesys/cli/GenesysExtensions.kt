@file:Suppress("MethodOverloading")

package com.nuecho.genesys.cli

import com.genesyslab.platform.applicationblocks.com.objects.CfgAccessGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlaceGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.applicationblocks.com.objects.CfgStatTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.commons.GEnum
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.configuration.protocol.types.CfgDNRegisterFlag
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgTransactionType
import com.genesyslab.platform.reporting.protocol.statserver.AgentStatus
import com.genesyslab.platform.reporting.protocol.statserver.DnActions
import com.genesyslab.platform.reporting.protocol.statserver.DnActionsMask
import com.genesyslab.platform.reporting.protocol.statserver.DnStatus
import com.genesyslab.platform.reporting.protocol.statserver.DnStatusesCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects
import com.nuecho.genesys.cli.models.configuration.reference.AccessGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.AgentGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.AgentLoginReference
import com.nuecho.genesys.cli.models.configuration.reference.ApplicationReference
import com.nuecho.genesys.cli.models.configuration.reference.DNGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.PhysicalSwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.PlaceGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.PlaceReference
import com.nuecho.genesys.cli.models.configuration.reference.ScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.SkillReference
import com.nuecho.genesys.cli.models.configuration.reference.StatTableReference
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference

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

fun GEnum.toShortName(): String =
    this.name().replace(ConfigurationObjects.CFG_PREFIX, "").toLowerCase()

fun CfgTransactionType.toShortName(): String =
    this.name().replace(ConfigurationObjects.CFG_TRANSACTION_PREFIX, "").toLowerCase()

fun CfgDNRegisterFlag.toShortName(): String =
    this.name().replace(ConfigurationObjects.CFG_DN_REGISTER_FLAG_PREFIX, "").toLowerCase()

fun CfgFlag.asBoolean(): Boolean? =
    when (this) {
        CfgFlag.CFGNoFlag -> null
        CfgFlag.CFGTrue -> true
        CfgFlag.CFGFalse -> false
        else -> throw IllegalArgumentException("Illegal CfgFlag value: '$this'")
    }

fun CfgAccessGroup.getReference() = AccessGroupReference(groupInfo.name)
fun CfgAgentGroup.getReference() = AgentGroupReference(groupInfo.name)
fun CfgAgentLogin.getReference() = AgentLoginReference(loginCode)
fun CfgApplication.getReference() = ApplicationReference(name)
fun CfgDN.getReference() = DNReference(this)
fun CfgDNGroup.getReference() = DNGroupReference(groupInfo.name)
fun CfgFolder.getReference() = FolderReference(name)
fun CfgObjectiveTable.getReference() = ObjectiveTableReference(name)
fun CfgPerson.getReference() = PersonReference(employeeID)
fun CfgPhysicalSwitch.getReference() = PhysicalSwitchReference(name)
fun CfgPlace.getReference() = PlaceReference(name)
fun CfgPlaceGroup.getReference() = PlaceGroupReference(groupInfo.name)
fun CfgScript.getReference() = ScriptReference(name)
fun CfgSkill.getReference() = SkillReference(name)
fun CfgStatTable.getReference() = StatTableReference(name)
fun CfgSwitch.getReference() = SwitchReference(name)
fun CfgTenant.getReference() = TenantReference(name)
