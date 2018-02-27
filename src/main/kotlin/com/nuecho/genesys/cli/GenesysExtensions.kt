package com.nuecho.genesys.cli

import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.commons.GEnum
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.reporting.protocol.statserver.AgentStatus
import com.genesyslab.platform.reporting.protocol.statserver.DnActions
import com.genesyslab.platform.reporting.protocol.statserver.DnActionsMask
import com.genesyslab.platform.reporting.protocol.statserver.DnStatus
import com.genesyslab.platform.reporting.protocol.statserver.DnStatusesCollection

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

fun CfgApplication.getDefaultEndpoint(): Endpoint? =
    if (this.serverInfo.host == null) null
    else Endpoint(this.serverInfo.host.name, this.getDefaultPort()!!)

fun CfgApplication.getDefaultPort(): Int? =
    this.portInfos.find { it.id == "default" }?.port?.toInt()

fun DnStatusesCollection.toList(): List<DnStatus> =
    (0 until this.count).map { this.getItem(it) }

fun DnStatusesCollection.toConsoleString(): String =
    this.toList().map { "DN: ${it.dnId} - Switch: ${it.switchId}" }.toString()

fun DnActionsMask.setBits(vararg dnActions: DnActions) =
    dnActions.forEach { setBit(it) }

fun GEnum.toShortName(): String =
    this.name().replace("CFG", "").toLowerCase()
