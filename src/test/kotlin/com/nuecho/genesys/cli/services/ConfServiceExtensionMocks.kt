package com.nuecho.genesys.cli.services

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.objects.CfgAppPrototype
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPCustomer
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPReseller
import com.genesyslab.platform.applicationblocks.com.objects.CfgHost
import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.applicationblocks.com.objects.CfgStatTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.applicationblocks.com.objects.CfgTimeZone
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGEnumerator
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgAppPrototype
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgGVPCustomer
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgGVPReseller
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgHost
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
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

    fun mockConfigurationObjectRepository() {
        val cfgFolder = mockCfgFolder("site", CfgObjectType.CFGFolder)
        every { ConfigurationObjectRepository.contains(any()) } returns false
        every { ConfigurationObjectRepository.contains(DEFAULT_FOLDER_REFERENCE) } returns true
        every { ConfigurationObjectRepository[DEFAULT_FOLDER_REFERENCE] } returns cfgFolder
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

    fun mockRetrieveEnumerator(service: IConfService, name: String?, dbid: Int = DEFAULT_OBJECT_DBID) {
        every { service.retrieveObject(CFGEnumerator, any()) } answers {
            val enumerator = mockk<CfgEnumerator>()
            every { enumerator.name } returns name
            every { enumerator.dbid } returns dbid
            every { enumerator.objectDbid } returns dbid
            enumerator
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

    fun mockRetrieveTimeZone(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) {
        every { service.retrieveObject(CfgTimeZone::class.java, any()) } answers {
            val switch = mockk<CfgTimeZone>()
            every { switch.dbid } returns dbid
            every { switch.objectDbid } returns dbid
            switch
        }
    }

    fun mockRetrieveTenant(service: IConfService) {
        every { service.retrieveObject(CfgTenant::class.java, any()) } answers {
            mockCfgTenant(DEFAULT_TENANT)
        }
    }

    fun mockRetrieveReseller(service: IConfService) {
        every { service.retrieveObject(CfgGVPReseller::class.java, any()) } answers {
            mockCfgGVPReseller(DEFAULT_TENANT)
        }
    }

    fun mockRetrieveCustomer(service: IConfService) {
        every { service.retrieveObject(CfgGVPCustomer::class.java, any()) } answers {
            mockCfgGVPCustomer(DEFAULT_TENANT)
        }
    }

    fun mockRetrieveHost(service: IConfService) =
        every { service.retrieveObject(CfgHost::class.java, any()) } answers {
            mockCfgHost(DEFAULT_TENANT)
        }

    fun mockRetrieveAppPrototype(service: IConfService) =
        every { service.retrieveObject(CfgAppPrototype::class.java, any()) } answers {
            mockCfgAppPrototype(DEFAULT_TENANT)
        }

    fun mockRetrieveFolderByDbid(service: IConfService) {
        val folder = mockCfgFolder()
        every { service.retrieveObject(CfgObjectType.CFGFolder, any()) } returns folder
    }
}
