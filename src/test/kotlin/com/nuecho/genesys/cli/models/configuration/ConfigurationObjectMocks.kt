package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgAccessGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLoginInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgAlarmCondition
import com.genesyslab.platform.applicationblocks.com.objects.CfgAppPrototype
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgCallingList
import com.genesyslab.platform.applicationblocks.com.objects.CfgCallingListInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgCampaign
import com.genesyslab.platform.applicationblocks.com.objects.CfgCampaignGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgDetectEvent
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumeratorValue
import com.genesyslab.platform.applicationblocks.com.objects.CfgField
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPCustomer
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPIVRProfile
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPReseller
import com.genesyslab.platform.applicationblocks.com.objects.CfgHost
import com.genesyslab.platform.applicationblocks.com.objects.CfgID
import com.genesyslab.platform.applicationblocks.com.objects.CfgIVR
import com.genesyslab.platform.applicationblocks.com.objects.CfgOS
import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgOwnerID
import com.genesyslab.platform.applicationblocks.com.objects.CfgParentID
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlaceGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgRemovalEvent
import com.genesyslab.platform.applicationblocks.com.objects.CfgRole
import com.genesyslab.platform.applicationblocks.com.objects.CfgRoleMember
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgService
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkillLevel
import com.genesyslab.platform.applicationblocks.com.objects.CfgStatTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgSubcode
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.applicationblocks.com.objects.CfgTimeZone
import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction
import com.genesyslab.platform.commons.collections.KeyValueCollection
import com.genesyslab.platform.commons.collections.KeyValuePair
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgFlag.CFGNoFlag
import com.genesyslab.platform.configuration.protocol.types.CfgIVRProfileType
import com.genesyslab.platform.configuration.protocol.types.CfgOSType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGFolder
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGTenant
import com.genesyslab.platform.configuration.protocol.types.CfgSelectionMode
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.CATEGORIZED_PROPERTIES_NUMBER
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.CATEGORIZED_PROPERTIES_STRING
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.OwnerReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.mockk

@Suppress("LargeClass")
object ConfigurationObjectMocks {
    const val DEFAULT_TENANT_NAME = "tenant"
    const val DEFAULT_FOLDER = "folder"
    const val DEFAULT_OBJECT_DBID = 101
    const val DEFAULT_TENANT_DBID = 1
    const val DEFAULT_FOLDER_DBID = 2
    const val SUBNAME = "subname"
    const val SUBCODE = "subcode"
    val DEFAULT_FOLDER_TYPE = CFGFolder
    val DEFAULT_IVR_PROFILE_TYPE = CfgIVRProfileType.CFGIPTOutbound
    val DEFAULT_TENANT_REFERENCE = TenantReference(DEFAULT_TENANT_NAME)
    val DEFAULT_FOLDER_REFERENCE = FolderReference(
        DEFAULT_FOLDER_TYPE.toShortName(),
        OwnerReference(CFGTenant.toShortName(), DEFAULT_TENANT_NAME),
        listOf(DEFAULT_FOLDER)
    )

    fun mockCfgAgentLoginInfo(loginCode: String, wrapupTime: Int): CfgAgentLoginInfo {
        val agentLogin = mockCfgAgentLogin(loginCode)
        return mockk<CfgAgentLoginInfo>().also {
            every { it.agentLogin } returns agentLogin
            every { it.wrapupTime } returns wrapupTime
        }
    }

    fun mockCfgSkillLevel(skillName: String, skillLevel: Int): CfgSkillLevel {
        val cfgSkill = mockCfgSkill(skillName)

        return mockk<CfgSkillLevel>().also {
            every { it.skill } returns cfgSkill
            every { it.level } returns skillLevel
        }
    }

    fun mockKeyValueCollection(): KeyValueCollection {
        val sectionKeyValueCollection = KeyValueCollection()
        with(sectionKeyValueCollection) {
            addPair(KeyValuePair("number", CATEGORIZED_PROPERTIES_NUMBER))
            addPair(KeyValuePair("string", CATEGORIZED_PROPERTIES_STRING))
        }

        val keyValueCollection = KeyValueCollection()
        with(keyValueCollection) {
            addPair(KeyValuePair("section", sectionKeyValueCollection))
        }

        return keyValueCollection
    }

    fun mockCfgAccessGroup(name: String = "name", tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME)) =
        mockk<CfgAccessGroup>().also {
            every { it.groupInfo.name } returns name
            every { it.groupInfo.tenant } returns tenant
            every { it.objectDbid } returns DEFAULT_OBJECT_DBID
        }

    fun mockCfgActionCode(name: String?, tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME)) =
        mockk<CfgActionCode>().also {
            every { it.name } returns name
            every { it.tenant } returns tenant
            every { it.objectDbid } returns DEFAULT_OBJECT_DBID
        }

    fun mockCfgSubcode(name: String? = SUBNAME, code: String? = SUBCODE) =
        mockk<CfgSubcode>().also {
            every { it.name } returns name
            every { it.code } returns code
        }

    fun mockCfgAgentGroup(
        name: String,
        tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME),
        dbid: Int = DEFAULT_OBJECT_DBID
    ) =
        mockk<CfgAgentGroup>().also {
            every { it.groupInfo.name } returns name
            every { it.groupInfo.tenant } returns tenant
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }

    fun mockCfgAgentLogin(
        loginCode: String,
        tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME),
        dbid: Int = DEFAULT_OBJECT_DBID
    ): CfgAgentLogin {
        val switch = mockCfgSwitch("switch")

        return mockk<CfgAgentLogin>().also {
            every { it.loginCode } returns loginCode
            every { it.switch } returns switch
            every { it.tenant } returns tenant
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }
    }

    fun mockCfgAlarmCondition(name: String?): CfgAlarmCondition =
        mockk<CfgAlarmCondition>().also {
            every { it.name } returns name
        }

    fun mockCfgDetectEvent(
        application: CfgApplication?,
        appType: CfgAppType?,
        logEventID: Int?,
        selectionMode: CfgSelectionMode?
    ) = mockk<CfgDetectEvent>().also {
        every { it.app } returns application
        every { it.appType } returns appType
        every { it.logEventID } returns logEventID
        every { it.selectionMode } returns selectionMode
    }

    fun mockCfgRemovalEvent(logEventID: Int?, selectionMode: CfgSelectionMode?) =
        mockk<CfgRemovalEvent>().also {
            every { it.logEventID } returns logEventID
            every { it.selectionMode } returns selectionMode
        }

    fun mockCfgApplication(name: String, dbid: Int = DEFAULT_OBJECT_DBID) =
        mockk<CfgApplication>().also {
            every { it.name } returns name
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }

    fun mockCfgAppPrototype(
        name: String?,
        dbid: Int = DEFAULT_OBJECT_DBID,
        type: CfgAppType? = null,
        version: String? = null
    ) =
        mockk<CfgAppPrototype>().also {
            every { it.name } returns name
            every { it.type } returns type
            every { it.version } returns version
            every { it.dbid } returns dbid
            every { it.objectDbid } returns dbid
        }

    fun mockCfgCampaign(
        name: String,
        tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME),
        dbid: Int = DEFAULT_OBJECT_DBID
    ) =
        mockk<CfgCampaign>().also {
            every { it.name } returns name
            every { it.tenant } returns tenant
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }

    fun mockCfgCampaignGroup(name: String, campaign: CfgCampaign = mockCfgCampaign("campaign")) =
        mockk<CfgCampaignGroup>().also {
            every { it.name } returns name
            every { it.campaign } returns campaign
            every { it.objectDbid } returns DEFAULT_OBJECT_DBID
        }

    fun mockCfgCallingList(name: String, dbid: Int = DEFAULT_OBJECT_DBID) =
        mockk<CfgCallingList>().also {
            every { it.name } returns name
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }

    fun mockCfgCallingListInfo(callingList: CfgCallingList?, isActive: CfgFlag? = CFGNoFlag, share: Int? = null) =
        mockk<CfgCallingListInfo>().also {
            every { it.callingList } returns callingList
            every { it.isActive } returns isActive
            every { it.share } returns share
        }

    fun mockCfgDN(
        number: String,
        type: CfgDNType = CfgDNType.CFGNoDN,
        tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME),
        switch: CfgSwitch? = null,
        dbid: Int = DEFAULT_OBJECT_DBID
    ) = mockk<CfgDN>().also {
        every { it.number } returns number
        every { it.type } returns type
        every { it.tenant } returns tenant
        every { it.objectDbid } returns dbid
        every { it.switch } returns switch
    }

    fun mockCfgDNGroup(
        name: String,
        tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME),
        dbid: Int = DEFAULT_OBJECT_DBID
    ) =
        mockk<CfgDNGroup>().also {
            every { it.groupInfo.name } returns name
            every { it.groupInfo.tenant } returns tenant
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }

    fun mockCfgDNInfo(trunks: Int = 0) =
        mockk<CfgDNInfo>().also {
            every { it.dndbid } returns DEFAULT_OBJECT_DBID
            every { it.trunks } returns trunks
        }

    fun mockCfgEnumerator(
        name: String?,
        tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME),
        dbid: Int = DEFAULT_OBJECT_DBID
    ) =
        mockk<CfgEnumerator>().also {
            every { it.tenant } returns tenant
            every { it.name } returns name
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }

    fun mockCfgEnumeratorValue(
        name: String?,
        enumerator: CfgEnumerator = mockCfgEnumerator("enumerator", mockCfgTenant(DEFAULT_TENANT_NAME))
    ) =
        mockk<CfgEnumeratorValue>().also {
            every { it.name } returns name
            every { it.enumerator } returns enumerator
        }

    fun mockCfgField(name: String?, tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME)) =
        mockk<CfgField>().also {
            every { it.tenant } returns tenant
            every { it.name } returns name
            every { it.objectDbid } returns DEFAULT_OBJECT_DBID
        }

    fun mockCfgFolder(
        name: String = DEFAULT_FOLDER,
        type: CfgObjectType = DEFAULT_FOLDER_TYPE,
        dbid: Int = DEFAULT_FOLDER_DBID
    ) =
        mockk<CfgFolder>().also {
            val owner = mockCfgOwnerID()
            val parent = mockCfgParentID()

            every { it.name } returns name
            every { it.type } returns type
            every { it.ownerID } returns owner
            every { it.parentID } returns parent
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }

    fun mockCfgOwnerID() =
        mockk<CfgOwnerID>().also {
            val tenant = ConfigurationObjectMocks.mockCfgTenant(DEFAULT_TENANT_NAME)

            val confService = mockConfService()
            every { confService.retrieveObject(CFGTenant, DEFAULT_TENANT_DBID) } returns tenant

            every { it.dbid } returns DEFAULT_TENANT_DBID
            every { it.type } returns CFGTenant
            every { it.configurationService } returns confService
        }

    fun mockCfgParentID() =
        mockk<CfgParentID>().also {
            every { it.getProperty("DBID") } returns DEFAULT_TENANT_DBID
            every { it.getProperty("type") } returns CFGTenant.ordinal()
        }

    fun mockCfgPlaceGroup(name: String?, tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME)) =
        mockk<CfgPlaceGroup>().also {
            every { it.groupInfo.name } returns name
            every { it.groupInfo.tenant } returns tenant
            every { it.objectDbid } returns DEFAULT_OBJECT_DBID
        }

    fun mockCfgID(type: CfgObjectType?) =
        mockk<CfgID>().also {
            every { it.dbid } returns DEFAULT_OBJECT_DBID
            every { it.type } returns type
        }

    fun mockCfgIvr(name: String?) =
        mockk<CfgIVR>().also {
            every { it.name } returns name
            every { it.dbid } returns DEFAULT_OBJECT_DBID
            every { it.objectDbid } returns DEFAULT_OBJECT_DBID
        }

    fun mockCfgHost(name: String, dbid: Int = DEFAULT_OBJECT_DBID) =
        mockk<CfgHost>().also {
            every { it.name } returns name
            every { it.dbid } returns dbid
            every { it.objectDbid } returns dbid
        }

    fun mockCfgObjectiveTable(
        name: String,
        tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME),
        dbid: Int = DEFAULT_OBJECT_DBID
    ) =
        mockk<CfgObjectiveTable>().also {
            every { it.name } returns name
            every { it.tenant } returns tenant
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }

    fun mockCfgOS(osType: CfgOSType?, osVersion: String?) =
        mockk<CfgOS>().also {
            every { it.oStype } returns osType
            every { it.oSversion } returns osVersion
        }

    fun mockCfgPerson(
        employeeID: String,
        tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME),
        dbid: Int = DEFAULT_OBJECT_DBID
    ) = mockk<CfgPerson>().also {
        every { it.employeeID } returns employeeID
        every { it.tenant } returns tenant
        every { it.objectDbid } returns dbid
        every { it.dbid } returns dbid
    }

    fun mockCfgPhysicalSwitch(name: String, dbid: Int = DEFAULT_OBJECT_DBID) =
        mockk<CfgPhysicalSwitch>().also {
            every { it.name } returns name
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }

    fun mockCfgPlace(
        name: String,
        tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME),
        dbid: Int = DEFAULT_OBJECT_DBID
    ) =
        mockk<CfgPlace>().also {
            every { it.name } returns name
            every { it.tenant } returns tenant
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }

    fun mockCfgRole(name: String?, tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME)) =
        mockk<CfgRole>().also {
            every { it.name } returns name
            every { it.tenant } returns tenant
            every { it.objectDbid } returns DEFAULT_OBJECT_DBID
        }

    fun mockCfgRoleMember(type: CfgObjectType? = CfgObjectType.CFGPerson) =
        mockk<CfgRoleMember>().also {
            every { it.objectDBID } returns DEFAULT_OBJECT_DBID
            every { it.objectType } returns type
        }

    fun mockCfgService(name: String?): CfgService =
        mockk<CfgService>().also {
            every { it.name } returns name
        }

    fun mockCfgScript(
        name: String,
        tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME),
        dbid: Int = DEFAULT_OBJECT_DBID
    ) =
        mockk<CfgScript>().also {
            every { it.name } returns name
            every { it.tenant } returns tenant
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }

    fun mockCfgSkill(
        name: String,
        tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME),
        dbid: Int = DEFAULT_OBJECT_DBID
    ) =
        mockk<CfgSkill>().also {
            every { it.name } returns name
            every { it.tenant } returns tenant
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }

    fun mockCfgGVPCustomer(
        name: String,
        tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME),
        dbid: Int = DEFAULT_OBJECT_DBID
    ) =
        mockk<CfgGVPCustomer>().also {
            every { it.name } returns name
            every { it.tenant } returns tenant
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }

    fun mockCfgSwitch(
        name: String,
        tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME),
        dbid: Int = DEFAULT_OBJECT_DBID
    ) =
        mockk<CfgSwitch>().also {
            every { it.name } returns name
            every { it.tenant } returns tenant
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }

    fun mockCfgStatTable(
        name: String,
        tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME),
        dbid: Int = DEFAULT_OBJECT_DBID
    ) =
        mockk<CfgStatTable>().also {
            every { it.name } returns name
            every { it.tenant } returns tenant
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }

    fun mockCfgTenant(name: String, dbid: Int = DEFAULT_TENANT_DBID) =
        mockk<CfgTenant>().also {
            every { it.name } returns name
            every { it.dbid } returns dbid
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }

    fun mockCfgTransaction(name: String?, tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME)) =
        mockk<CfgTransaction>().also {
            every { it.name } returns name
            every { it.tenant } returns tenant
            every { it.objectDbid } returns DEFAULT_OBJECT_DBID
        }

    fun mockCfgGVPReseller(
        name: String,
        tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME),
        dbid: Int = DEFAULT_OBJECT_DBID
    ) =
        mockk<CfgGVPReseller>().also {
            every { it.name } returns name
            every { it.tenant } returns tenant
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }

    fun mockCfgGVPIVRProfile(name: String?, tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME)) =
        mockk<CfgGVPIVRProfile>().also {
            every { it.name } returns name
            every { it.tenant } returns tenant
            every { it.objectDbid } returns DEFAULT_OBJECT_DBID
        }

    fun mockCfgTimeZone(
        name: String,
        tenant: CfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME),
        dbid: Int = DEFAULT_OBJECT_DBID
    ) =
        mockk<CfgTimeZone>().also {
            every { it.name } returns name
            every { it.tenant } returns tenant
            every { it.objectDbid } returns dbid
            every { it.dbid } returns dbid
        }
}
