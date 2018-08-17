package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAlarmCondition
import com.genesyslab.platform.applicationblocks.com.objects.CfgDetectEvent
import com.genesyslab.platform.applicationblocks.com.objects.CfgRemovalEvent
import com.genesyslab.platform.configuration.protocol.types.CfgAppType.CFGNoApplication
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgAlarmCategory
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgAppType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgSelectionMode
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.AlarmConditionReference
import com.nuecho.genesys.cli.models.configuration.reference.AlarmConditionScriptReference
import com.nuecho.genesys.cli.models.configuration.reference.ApplicationReference
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

data class AlarmCondition(
    val name: String,
    val alarmDetectEvent: DetectEvent? = null,
    val alarmDetectScript: AlarmConditionScriptReference? = null,
    val alarmRemovalEvent: RemovalEvent? = null,
    val category: String? = null,
    val clearanceScripts: List<AlarmConditionScriptReference>? = null,
    val clearanceTimeout: Int? = null,
    val description: String? = null,
    @get:JsonProperty("isMasked")
    val isMasked: Boolean? = null,
    val reactionScripts: List<AlarmConditionScriptReference>? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = AlarmConditionReference(name)

    constructor(alarmCondition: CfgAlarmCondition) : this(
        name = alarmCondition.name,
        alarmDetectEvent = alarmCondition.alarmDetectEvent?.run {
            DetectEvent(
                app?.getReference(),
                appType?.toShortName(),
                logEventID,
                selectionMode?.toShortName()
            )
        },
        alarmDetectScript = alarmCondition.alarmDetectScript?.run {
            AlarmConditionScriptReference(name, tenant.getReference())
        },
        alarmRemovalEvent = alarmCondition.alarmRemovalEvent?.run {
            RemovalEvent(logEventID, selectionMode?.toShortName())
        },
        category = alarmCondition.category?.toShortName(),
        clearanceScripts = alarmCondition.clearanceScripts?.map {
            AlarmConditionScriptReference(it.name, it.tenant.getReference())
        },
        clearanceTimeout = alarmCondition.clearanceTimeout,
        description = alarmCondition.description,
        isMasked = alarmCondition.isMasked?.asBoolean(),
        reactionScripts = alarmCondition.reactionScripts?.map {
            AlarmConditionScriptReference(it.name, it.tenant.getReference())
        },
        state = alarmCondition.state?.toShortName(),
        folder = alarmCondition.getFolderReference(),
        userProperties = alarmCondition.userProperties.asCategorizedProperties()
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgAlarmCondition(service).also {
            applyDefaultValues()
            setProperty("name", name, it)
            setFolder(folder, it)
        })

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgAlarmCondition).also {
            setProperty(
                ALARM_DETECT_EVENT,
                alarmDetectEvent?.toUpdatedCfgDetectEvent(service, it.alarmDetectEvent ?: CfgDetectEvent(service, it)),
                it
            )
            setProperty(
                "alarmDetectScriptDBID",
                alarmDetectScript?.let { service.getObjectDbid(it) },
                it
            )
            setProperty(
                "alarmRemovalEvent",
                alarmRemovalEvent?.toUpdatedCfgRemovalEvent(it.alarmRemovalEvent ?: CfgRemovalEvent(service, it)),
                it
            )
            setProperty(CATEGORY, toCfgAlarmCategory(category), it)
            setProperty("clearanceScriptDBIDs", clearanceScripts?.map { service.getObjectDbid(it) }, it)
            setProperty("clearanceTimeout", clearanceTimeout, it)
            setProperty("description", description, it)
            setProperty("isMasked", toCfgFlag(isMasked), it)
            setProperty(
                "reactionScriptDBIDs",
                reactionScripts?.map { reactionScript -> service.getObjectDbid(reactionScript) },
                it
            )
            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
        }

    override fun cloneBare() = AlarmCondition(
        name = name,
        alarmDetectEvent = alarmDetectEvent,
        category = category
    )

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> {
        val missingMandatoryProperties = mutableSetOf<String>()
        alarmDetectEvent ?: missingMandatoryProperties.add(ALARM_DETECT_EVENT)
        alarmDetectEvent?.logEventID ?: missingMandatoryProperties.add(ALARM_DETECT_EVENT_LOG_EVENT_ID)
        category ?: missingMandatoryProperties.add(CATEGORY)

        return missingMandatoryProperties
    }

    override fun checkUnchangeableProperties(cfgObject: CfgObject) = checkUnchangeableProperties(this, cfgObject)

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(alarmDetectEvent?.app)
            .add(alarmDetectScript?.toScriptReference())
            .add(clearanceScripts?.map { it.toScriptReference() })
            .add(reactionScripts?.map { it.toScriptReference() })
            .add(folder)
            .toSet()
}

data class DetectEvent(
    val app: ApplicationReference? = null,
    val appType: String? = null,
    val logEventID: Int? = null,
    val selectionMode: String? = null
) {
    fun toUpdatedCfgDetectEvent(service: IConfService, cfgDetectEvent: CfgDetectEvent) = cfgDetectEvent.also {
        setProperty("appDBID", service.getObjectDbid(app), it)
        // CFGNoApplication is the default value set by the config server
        setProperty("appType", appType?.let { toCfgAppType(it) } ?: CFGNoApplication, it)
        setProperty("logEventID", logEventID, it)
        setProperty("selectionMode", toCfgSelectionMode(selectionMode), it)
    }
}

data class RemovalEvent(
    val logEventID: Int? = null,
    val selectionMode: String? = null
) {
    fun toUpdatedCfgRemovalEvent(cfgRemovalEvent: CfgRemovalEvent) = cfgRemovalEvent.also {
        setProperty("logEventID", logEventID, it)
        setProperty("selectionMode", toCfgSelectionMode(selectionMode), it)
    }
}
