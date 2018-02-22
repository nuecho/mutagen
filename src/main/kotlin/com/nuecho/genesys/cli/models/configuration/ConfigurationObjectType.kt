package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.CfgQuery
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.queries.CfgAccessGroupQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgActionCodeQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgAgentGroupQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgAgentLoginQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgAlarmConditionQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgAppPrototypeQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgApplicationQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgCallingListQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgCampaignGroupQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgCampaignQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgDNGroupQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgDNQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgEnumeratorQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgEnumeratorValueQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgFieldQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgFilterQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgFolderQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgFormatQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgGVPIVRProfileQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgHostQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgIVRPortQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgIVRQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgObjectiveTableQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPersonQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPhysicalSwitchQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPlaceGroupQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPlaceQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgRoleQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgScheduledTaskQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgScriptQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgServiceQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgSkillQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgStatDayQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgStatTableQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgSwitchQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgTableAccessQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgTenantQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgTimeZoneQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgTransactionQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgTreatmentQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgVoicePromptQuery
import kotlin.reflect.KClass

enum class ConfigurationObjectType(
    val queryType: KClass<out CfgQuery>,
    private val primaryKey: String = "name",
    private val group: Boolean = false
) {
    ACCESS_GROUP(CfgAccessGroupQuery::class, group = true),
    ACTION_CODE(CfgActionCodeQuery::class),
    AGENT_GROUP(CfgAgentGroupQuery::class, group = true),
    AGENT_LOGIN(CfgAgentLoginQuery::class, "loginCode"),
    ALARM_CONDITION(CfgAlarmConditionQuery::class),
    APPLICATION(CfgApplicationQuery::class),
    APP_PROTOTYPE(CfgAppPrototypeQuery::class),
    CALLING_LIST(CfgCallingListQuery::class),
    CAMPAIGN(CfgCampaignQuery::class),
    CAMPAIGN_GROUP(CfgCampaignGroupQuery::class),
    DN(CfgDNQuery::class, "number"),
    DN_GROUP(CfgDNGroupQuery::class, group = true),
    ENUMERATOR(CfgEnumeratorQuery::class),
    ENUMERATOR_VALUE(CfgEnumeratorValueQuery::class),
    FIELD(CfgFieldQuery::class),
    FILTER(CfgFilterQuery::class),
    FOLDER(CfgFolderQuery::class),
    FORMAT(CfgFormatQuery::class),
    GVP_IVR_PROFILE(CfgGVPIVRProfileQuery::class),
    HOST(CfgHostQuery::class),
    IVR(CfgIVRQuery::class),
    IVR_PORT(CfgIVRPortQuery::class, "portNumber"),
    OBJECTIVE_TABLE(CfgObjectiveTableQuery::class),
    PERSON(CfgPersonQuery::class, "employeeID"),
    PHYSICAL_SWITCH(CfgPhysicalSwitchQuery::class),
    PLACE(CfgPlaceQuery::class),
    PLACE_GROUP(CfgPlaceGroupQuery::class, group = true),
    ROLE(CfgRoleQuery::class),
    SCHEDULED_TASK(CfgScheduledTaskQuery::class),
    SCRIPT(CfgScriptQuery::class),
    SERVICE(CfgServiceQuery::class),
    SKILL(CfgSkillQuery::class),
    STAT_DAY(CfgStatDayQuery::class),
    STAT_TABLE(CfgStatTableQuery::class),
    SWITCH(CfgSwitchQuery::class),
    TABLE_ACCESS(CfgTableAccessQuery::class),
    TENANT(CfgTenantQuery::class),
    TIME_ZONE(CfgTimeZoneQuery::class),
    TRANSACTION(CfgTransactionQuery::class),
    TREATMENT(CfgTreatmentQuery::class),
    VOICE_PROMPT(CfgVoicePromptQuery::class);

    fun getObjectType() = queryType.simpleName!!.replace(Regex("Query"), "")

    fun getObjectId(configurationObject: ICfgObject): String {
        if (group) {
            val groupInfoGetter = configurationObject.javaClass.getMethod("getGroupInfo")
            val groupInfo = groupInfoGetter.invoke(configurationObject)
            return getStringProperty(groupInfo, primaryKey)
        }

        return getStringProperty(configurationObject, primaryKey)
    }

    private fun getStringProperty(target: Any, propertyName: String): String {
        val getterName = "get" + propertyName.capitalize()
        val getter = target.javaClass.getMethod(getterName)
        return getter.invoke(target) as String
    }
}
