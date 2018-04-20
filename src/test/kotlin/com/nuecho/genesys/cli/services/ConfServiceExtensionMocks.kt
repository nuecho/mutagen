package com.nuecho.genesys.cli.services

import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.applicationblocks.com.objects.CfgStatTable
import io.mockk.every
import io.mockk.mockk

object ConfServiceExtensionMocks {
    fun mockRetrieveScript(service: ConfService, dbid: Int) =
        every { service.retrieveScript(any()) } answers {
            val script = mockk<CfgScript>()
            every { script.dbid } returns dbid
            script
        }

    fun mockRetrieveObjectiveTable(service: ConfService, dbid: Int) =
        every { service.retrieveObjectiveTable(any()) } answers {
            val objectiveTable = mockk<CfgObjectiveTable>()
            every { objectiveTable.dbid } returns dbid
            objectiveTable
        }

    fun mockRetrievePlace(service: ConfService, dbid: Int) =
        every { service.retrievePlace(any()) } answers {
            val place = mockk<CfgPlace>()
            every { place.dbid } returns dbid
            place
        }

    fun mockRetrieveFolder(service: ConfService, dbid: Int) =
        every { service.retrieveFolder(any()) } answers {
            val folder = mockk<CfgFolder>()
            every { folder.dbid } returns dbid
            folder
        }

    fun mockRetrieveSkill(service: ConfService, dbid: Int) =
        every { service.retrieveSkill(any()) } answers {
            val skill = mockk<CfgSkill>()
            every { skill.dbid } returns dbid
            skill
        }

    fun mockRetrieveAgentLogin(service: ConfService, dbid: Int) =
        every { service.retrieveAgentLogin(any()) } answers {
            val agentLogin = mockk<CfgAgentLogin>()
            every { agentLogin.dbid } returns dbid
            agentLogin
        }

    fun mockRetrievePerson(service: ConfService, dbid: Int) {
        every { service.retrievePerson(any()) } answers {
            val person = mockk<CfgPerson>()
            every { person.dbid } returns dbid
            person
        }
    }

    fun mockRetrieveDN(service: ConfService, dbid: Int) {
        every { service.retrieveDN(any()) } answers {
            val dn = mockk<CfgDN>()
            every { dn.dbid } returns dbid
            dn
        }
    }

    fun mockRetrieveStatTable(service: ConfService, dbid: Int) {
        every { service.retrieveStatTable(any()) } answers {
            val statTable = mockk<CfgStatTable>()
            every { statTable.dbid } returns dbid
            statTable
        }
    }
}
