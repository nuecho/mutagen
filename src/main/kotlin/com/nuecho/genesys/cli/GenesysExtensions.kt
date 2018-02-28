package com.nuecho.genesys.cli

import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.commons.GEnum
import com.genesyslab.platform.commons.collections.KeyValueCollection
import com.genesyslab.platform.commons.collections.KeyValuePair
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
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
    this.name().replace("CFG", "").toLowerCase()

fun CfgFlag.asBoolean(): Boolean? =
    when (this) {
        CfgFlag.CFGNoFlag -> null
        CfgFlag.CFGTrue -> true
        CfgFlag.CFGFalse -> false
        else -> throw IllegalArgumentException("Illegal CfgFlag value: '$this'")
    }

fun KeyValueCollection.asMap(): Map<String, Any> =
    this.map {
        val keyValuePair = it as KeyValuePair
        val value = keyValuePair.value!!
        keyValuePair.stringKey!! to if (value is KeyValueCollection) value.asMap() else value
    }.toMap()
