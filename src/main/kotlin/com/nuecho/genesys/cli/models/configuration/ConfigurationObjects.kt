package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.ICfgBase
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgIVRPort
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.commons.GEnum
import com.genesyslab.platform.commons.collections.KeyValueCollection
import com.genesyslab.platform.commons.collections.KeyValuePair
import com.genesyslab.platform.configuration.protocol.types.CfgActionCodeType
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.genesyslab.platform.configuration.protocol.types.CfgEnumeratorType
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgFlag.CFGFalse
import com.genesyslab.platform.configuration.protocol.types.CfgFlag.CFGNoFlag
import com.genesyslab.platform.configuration.protocol.types.CfgFlag.CFGTrue
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.genesyslab.platform.configuration.protocol.types.CfgRank

private const val CFG_PREFIX = "CFG"

object ConfigurationObjects {
    fun getCfgObjectTypes(): Set<CfgObjectType> {
        val types = GEnum.valuesBy(CfgObjectType::class.java).toMutableSet()
        types.remove(CfgObjectType.CFGNoObject)
        types.remove(CfgObjectType.CFGMaxObjectType)
        return types
    }

    fun setProperty(name: String, value: Any?, cfgBase: ICfgBase) =
        value?.let { cfgBase.setProperty(name, it) }

    fun getPrimaryKey(configurationObject: ICfgObject?): String? {
        if (configurationObject == null) return null

        return try {
            val groupInfoGetter = configurationObject.javaClass.getMethod("getGroupInfo")
            val groupInfo = groupInfoGetter.invoke(configurationObject)
            getStringProperty(groupInfo, "name")
        } catch (exception: Exception) {
            // Not a group
            getStringProperty(configurationObject, getPrimaryKeyProperty(configurationObject))
        }
    }

    fun dbidToPrimaryKey(dbid: Int, type: CfgObjectType, service: IConfService) =
        getPrimaryKey(service.retrieveObject(type, dbid))

    @Suppress("UNCHECKED_CAST")
    fun toKeyValueCollection(map: Map<String, Any>?): KeyValueCollection? =
        if (map == null) null
        else {
            val keyValueCollection = KeyValueCollection()

            map.mapTo(keyValueCollection) { (key, value) ->
                when (value) {
                    is Int -> KeyValuePair(key, value)
                    is String -> KeyValuePair(key, value)
                    is ByteArray -> KeyValuePair(key, value)
                    is Map<*, *> -> KeyValuePair(key, toKeyValueCollection(value as Map<String, Any>))
                    else -> throw AssertionError("Unsupported KeyValueCollection value ($value)")
                }
            }

            keyValueCollection
        }

    fun toCfgFlag(flag: Boolean?): CfgFlag = when (flag) {
        true -> CFGTrue
        false -> CFGFalse
        else -> CFGNoFlag
    }

    fun toCfgActionCodeType(state: String?) = toGEnum(state, CfgActionCodeType::class.java) as CfgActionCodeType?
    fun toCfgAppType(type: String?) = toGEnum(type, CfgAppType::class.java) as CfgAppType?
    fun toCfgEnumeratorType(type: String?) = toGEnum(type, CfgEnumeratorType::class.java) as CfgEnumeratorType?
    fun toCfgObjectState(state: String?) = toGEnum(state, CfgObjectState::class.java) as CfgObjectState?
    fun toCfgRank(rank: String?) = toGEnum(rank, CfgRank::class.java) as CfgRank?

    private fun toGEnum(shortName: String?, enumType: Class<out GEnum>) =
        if (shortName == null) null
        else GEnum.getValue(enumType, "$CFG_PREFIX$shortName")

    private fun getPrimaryKeyProperty(target: Any) = when (target) {
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
}
