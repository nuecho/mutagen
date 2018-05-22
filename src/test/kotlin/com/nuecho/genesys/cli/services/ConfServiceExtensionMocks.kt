package com.nuecho.genesys.cli.services

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.applicationblocks.com.objects.CfgStatTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_DBID
import io.mockk.every
import io.mockk.mockk

object ConfServiceExtensionMocks {
    fun mockRetrieveApplication(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgApplication::class.java, any()) } answers {
            val application = mockk<CfgApplication>()
            every { application.dbid } returns dbid
            every { application.objectDbid } returns dbid
            application
        }

    fun mockRetrieveScript(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgScript::class.java, any()) } answers {
            val script = mockk<CfgScript>()
            every { script.dbid } returns dbid
            every { script.objectDbid } returns dbid
            script
        }

    fun mockRetrieveObjectiveTable(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgObjectiveTable::class.java, any()) } answers {
            val objectiveTable = mockk<CfgObjectiveTable>()
            every { objectiveTable.dbid } returns dbid
            every { objectiveTable.objectDbid } returns dbid
            objectiveTable
        }

    fun mockRetrievePhysicalSwitch(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } answers {
            val physicalSwitch = mockk<CfgPhysicalSwitch>()
            every { physicalSwitch.dbid } returns dbid
            every { physicalSwitch.objectDbid } returns dbid
            physicalSwitch
        }

    fun mockRetrievePlace(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgPlace::class.java, any()) } answers {
            val place = mockk<CfgPlace>()
            every { place.dbid } returns dbid
            every { place.objectDbid } returns dbid
            place
        }

    fun mockRetrieveFolder(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgFolder::class.java, any()) } answers {
            val folder = mockk<CfgFolder>()
            every { folder.dbid } returns dbid
            every { folder.objectDbid } returns dbid
            folder
        }

    fun mockRetrieveSkill(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgSkill::class.java, any()) } answers {
            val skill = mockk<CfgSkill>()
            every { skill.dbid } returns dbid
            every { skill.objectDbid } returns dbid
            skill
        }

    fun mockRetrieveAgentLogin(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgAgentLogin::class.java, any()) } answers {
            val agentLogin = mockk<CfgAgentLogin>()
            every { agentLogin.dbid } returns dbid
            every { agentLogin.objectDbid } returns dbid
            agentLogin
        }

    fun mockRetrievePerson(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) {
        every { service.retrieveObject(CfgPerson::class.java, any()) } answers {
            val person = mockk<CfgPerson>()
            every { person.dbid } returns dbid
            every { person.objectDbid } returns dbid
            person
        }
    }

    fun mockRetrieveDN(service: IConfService, cfgSwitch: CfgSwitch, dbid: Int = DEFAULT_OBJECT_DBID) {
        every { service.retrieveObject(CfgDN::class.java, any()) } answers {
            val dn = mockk<CfgDN>()
            every { dn.dbid } returns dbid
            every { dn.objectDbid } returns dbid
            every { dn.switch } returns cfgSwitch
            dn
        }
    }

    fun mockRetrieveDNGroup(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) {
        every { service.retrieveObject(CfgDNGroup::class.java, any()) } answers {
            val dnGroup = mockk<CfgDNGroup>()
            every { dnGroup.dbid } returns dbid
            every { dnGroup.objectDbid } returns dbid
            dnGroup
        }
    }

    fun mockRetrieveStatTable(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) {
        every { service.retrieveObject(CfgStatTable::class.java, any()) } answers {
            val statTable = mockk<CfgStatTable>()
            every { statTable.dbid } returns dbid
            every { statTable.objectDbid } returns dbid
            statTable
        }
    }

    fun mockRetrieveSwitch(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) {
        every { service.retrieveObject(CfgSwitch::class.java, any()) } answers {
            val switch = mockk<CfgSwitch>()
            every { switch.dbid } returns dbid
            every { switch.objectDbid } returns dbid
            switch
        }
    }

    fun mockRetrieveTenant(service: IConfService, dbid: Int = DEFAULT_TENANT_DBID) {
        every { service.retrieveObject(CfgTenant::class.java, any()) } answers {
            val tenant = mockk<CfgTenant>()
            every { tenant.dbid } returns dbid
            every { tenant.objectDbid } returns dbid
            tenant
        }
    }
}
