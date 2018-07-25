package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgBase
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.commons.GEnum
import com.genesyslab.platform.commons.collections.KeyValueCollection
import com.genesyslab.platform.commons.collections.KeyValuePair
import com.genesyslab.platform.configuration.protocol.types.CfgAccessGroupType
import com.genesyslab.platform.configuration.protocol.types.CfgActionCodeType
import com.genesyslab.platform.configuration.protocol.types.CfgAlarmCategory
import com.genesyslab.platform.configuration.protocol.types.CfgAppComponentType
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.genesyslab.platform.configuration.protocol.types.CfgDNGroupType
import com.genesyslab.platform.configuration.protocol.types.CfgDNRegisterFlag
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.genesyslab.platform.configuration.protocol.types.CfgDataType
import com.genesyslab.platform.configuration.protocol.types.CfgDialMode
import com.genesyslab.platform.configuration.protocol.types.CfgEnumeratorType
import com.genesyslab.platform.configuration.protocol.types.CfgFieldType
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgFlag.CFGFalse
import com.genesyslab.platform.configuration.protocol.types.CfgFlag.CFGNoFlag
import com.genesyslab.platform.configuration.protocol.types.CfgFlag.CFGTrue
import com.genesyslab.platform.configuration.protocol.types.CfgFolderClass
import com.genesyslab.platform.configuration.protocol.types.CfgHAType
import com.genesyslab.platform.configuration.protocol.types.CfgHostType
import com.genesyslab.platform.configuration.protocol.types.CfgIVRProfileType
import com.genesyslab.platform.configuration.protocol.types.CfgLinkType
import com.genesyslab.platform.configuration.protocol.types.CfgOSType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGMaxObjectType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGNoObject
import com.genesyslab.platform.configuration.protocol.types.CfgOperationMode
import com.genesyslab.platform.configuration.protocol.types.CfgOptimizationMethod
import com.genesyslab.platform.configuration.protocol.types.CfgRank
import com.genesyslab.platform.configuration.protocol.types.CfgRouteType
import com.genesyslab.platform.configuration.protocol.types.CfgScriptType
import com.genesyslab.platform.configuration.protocol.types.CfgSelectionMode
import com.genesyslab.platform.configuration.protocol.types.CfgSolutionType
import com.genesyslab.platform.configuration.protocol.types.CfgStartupType
import com.genesyslab.platform.configuration.protocol.types.CfgSwitchType
import com.genesyslab.platform.configuration.protocol.types.CfgTargetType
import com.genesyslab.platform.configuration.protocol.types.CfgTraceMode
import com.genesyslab.platform.configuration.protocol.types.CfgTransactionType
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference

object ConfigurationObjects {

    const val CFG_PREFIX = "CFG"
    const val CFG_TRANSACTION_PREFIX = "${CFG_PREFIX}TRT"
    const val CFG_DN_REGISTER_FLAG_PREFIX = "${CFG_PREFIX}DR"
    const val CFG_IVR_PROFILE_PREFIX = "${CFG_PREFIX}IPT"
    const val CFG_FOLDER_CLASS_PREFIX = "${CFG_PREFIX}FC"

    fun getCfgObjectTypes(): Set<CfgObjectType> {
        val types = GEnum.valuesBy(CfgObjectType::class.java).toMutableSet()
        types.remove(CFGNoObject)
        types.remove(CFGMaxObjectType)
        return types
    }

    fun getCfgObjectType(cfgObjectClass: Class<out ICfgObject>) =
        CfgObjectType.getValue(CfgObjectType::class.java, cfgObjectClass.simpleName) as CfgObjectType

    fun setFolder(folder: FolderReference?, cfgObject: CfgObject) =
        folder?.let { cfgObject.folderId = folder.toFolderDbid(cfgObject.configurationService) }

    fun setProperty(name: String, value: Any?, cfgBase: ICfgBase) =
        value?.let { cfgBase.setProperty(name, it) }

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

    fun toCfgDNRegisterFlag(flag: String?) =
        if (flag == null) null
        else GEnum.getValue(CfgDNRegisterFlag::class.java, "$CFG_DN_REGISTER_FLAG_PREFIX$flag") as CfgDNRegisterFlag

    fun toCfgAccessGroupType(type: String?) = toGEnum(type, CfgAccessGroupType::class.java) as CfgAccessGroupType?
    fun toCfgActionCodeType(type: String?) = toGEnum(type, CfgActionCodeType::class.java) as CfgActionCodeType?
    fun toCfgAlarmCategory(category: String?) = toGEnum(category, CfgAlarmCategory::class.java) as CfgAlarmCategory?
    fun toCfgAppComponentType(type: String?) = toGEnum(type, CfgAppComponentType::class.java) as CfgAppComponentType?
    fun toCfgAppType(type: String?) = toGEnum(type, CfgAppType::class.java) as CfgAppType?
    fun toCfgDataType(type: String?) = toGEnum(type, CfgDataType::class.java) as CfgDataType?
    fun toCfgDialMode(mode: String?) = toGEnum(mode, CfgDialMode::class.java) as CfgDialMode?
    fun toCfgDNGroupType(type: String?) = toGEnum(type, CfgDNGroupType::class.java) as CfgDNGroupType?
    fun toCfgDNType(type: String?) = toGEnum(type, CfgDNType::class.java) as CfgDNType?
    fun toCfgEnumeratorType(type: String?) = toGEnum(type, CfgEnumeratorType::class.java) as CfgEnumeratorType?
    fun toCfgFieldType(type: String?) = toGEnum(type, CfgFieldType::class.java) as CfgFieldType?
    fun toCfgHAType(type: String?) = toGEnum(type, CfgHAType::class.java) as CfgHAType?
    fun toCfgHostType(type: String?) = toGEnum(type, CfgHostType::class.java) as CfgHostType?
    fun toCfgLinkType(type: String?) = toGEnum(type, CfgLinkType::class.java) as CfgLinkType?
    fun toCfgObjectState(state: String?) = toGEnum(state, CfgObjectState::class.java) as CfgObjectState?
    fun toCfgObjectType(type: String?) = toGEnum(type, CfgObjectType::class.java) as CfgObjectType?
    fun toCfgOperationMode(mode: String?) = toGEnum(mode, CfgOperationMode::class.java) as CfgOperationMode?
    fun toCfgOptimizationMethod(method: String?) =
        toGEnum(method, CfgOptimizationMethod::class.java) as CfgOptimizationMethod?

    fun toCfgOsType(type: String?) = toGEnum(type, CfgOSType::class.java) as CfgOSType?
    fun toCfgRank(rank: String?) = toGEnum(rank, CfgRank::class.java) as CfgRank?
    fun toCfgRouteType(type: String?) = toGEnum(type, CfgRouteType::class.java) as CfgRouteType?
    fun toCfgScriptType(type: String?) = toGEnum(type, CfgScriptType::class.java) as CfgScriptType?
    fun toCfgSelectionMode(selectionMode: String?) =
        toGEnum(selectionMode, CfgSelectionMode::class.java) as CfgSelectionMode?

    fun toCfgSolutionType(type: String?) = toGEnum(type, CfgSolutionType::class.java) as CfgSolutionType?
    fun toCfgStartupType(type: String?) = toGEnum(type, CfgStartupType::class.java) as CfgStartupType?
    fun toCfgSwitchType(type: String?) = toGEnum(type, CfgSwitchType::class.java) as CfgSwitchType?
    fun toCfgTargetType(type: String?) = toGEnum(type, CfgTargetType::class.java) as CfgTargetType?
    fun toCfgTraceMode(mode: String?) = toGEnum(mode, CfgTraceMode::class.java) as CfgTraceMode?

    fun toCfgIVRProfileType(type: String?) =
        if (type == null) null
        else GEnum.getValue(CfgIVRProfileType::class.java, "$CFG_IVR_PROFILE_PREFIX$type") as CfgIVRProfileType

    fun toCfgTransactionType(transactionType: String?) =
        if (transactionType == null) null
        else GEnum.getValue(CfgTransactionType::class.java, "$CFG_TRANSACTION_PREFIX$transactionType")
                as CfgTransactionType

    fun toCfgFolderClass(folderClass: String?) =
        if (folderClass == null) null
        else GEnum.getValue(CfgFolderClass::class.java, "$CFG_FOLDER_CLASS_PREFIX$folderClass") as CfgFolderClass

    private fun toGEnum(shortName: String?, enumType: Class<out GEnum>) =
        if (shortName == null) null
        else GEnum.getValue(enumType, "$CFG_PREFIX$shortName")
}
