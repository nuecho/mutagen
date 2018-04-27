package com.nuecho.genesys.cli

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgIVRPort
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.commons.GEnum
import com.genesyslab.platform.commons.collections.KeyValueCollection
import com.genesyslab.platform.commons.collections.KeyValuePair
import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgTransactionType
import com.genesyslab.platform.reporting.protocol.statserver.AgentStatus
import com.genesyslab.platform.reporting.protocol.statserver.DnActions
import com.genesyslab.platform.reporting.protocol.statserver.DnActionsMask
import com.genesyslab.platform.reporting.protocol.statserver.DnStatus
import com.genesyslab.platform.reporting.protocol.statserver.DnStatusesCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects

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

fun CfgFlag.asBoolean(): Boolean? =
    when (this) {
        CfgFlag.CFGNoFlag -> null
        CfgFlag.CFGTrue -> true
        CfgFlag.CFGFalse -> false
        else -> throw IllegalArgumentException("Illegal CfgFlag value: '$this'")
    }

fun KeyValueCollection.asMap(): Map<String, Any>? =
    if (this.isEmpty()) null // so we don't turn out serializing top level empty map
    else this.map {
        val keyValuePair = it as KeyValuePair
        var value = keyValuePair.value!!

        if (value is KeyValueCollection) {
            value = value.asMap() ?: emptyMap<String, String>()
        }

        keyValuePair.stringKey!! to value
    }.toMap()

fun ICfgObject.getPrimaryKey(): String = try {
    val groupInfoGetter = this.javaClass.getMethod("getGroupInfo")
    val groupInfo = groupInfoGetter.invoke(this) as CfgGroup
    groupInfo.name
} catch (exception: Exception) {
    // Not a group
    getStringProperty(this, getPrimaryKeyProperty(this))
}

fun Collection<ICfgObject?>.toPrimaryKeyList() =
    this.mapNotNull { it?.getPrimaryKey() }

private fun getPrimaryKeyProperty(target: ICfgObject?) = when (target) {
    is CfgAgentLogin -> "loginCode"
    is CfgDN -> "number"
    is CfgIVRPort -> "portNumber"
    is CfgPerson -> "employeeID"
    else -> "name"
}

private fun getStringProperty(target: Any, propertyName: String): String {
    val getterName = "get${propertyName.capitalize()}"
    val getter = target.javaClass.getMethod(getterName)
    return getter.invoke(target) as String
}
