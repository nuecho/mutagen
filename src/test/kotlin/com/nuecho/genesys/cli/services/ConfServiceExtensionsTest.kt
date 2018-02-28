package com.nuecho.genesys.cli.services

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgQuery
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.queries.CfgAgentLoginQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgFolderQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgObjectiveTableQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPersonQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPlaceQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgScriptQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgSkillQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgSwitchQuery
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class ConfServiceExtensionsTest : StringSpec() {
    init {
        val service = mockk<ConfService>()
        val type = slot<Class<out ICfgObject>>()
        val query = slot<ICfgQuery>()

        every {
            service.retrieveObject(capture(type), capture(query))
        } returns null

        "retrieveAgentLogin should create the proper query " {
            val loginCode = "loginCode"
            service.retrieveAgentLogin(loginCode) shouldBe null
            type.captured shouldBe CfgAgentLogin::class.java
            query.captured.javaClass shouldBe CfgAgentLoginQuery::class.java
            (query.captured as CfgAgentLoginQuery).loginCode shouldBe loginCode
        }

        "retrieveFolder should create the proper query " {
            val folderName = "folder"
            service.retrieveFolder(folderName) shouldBe null
            type.captured shouldBe CfgFolder::class.java
            query.captured.javaClass shouldBe CfgFolderQuery::class.java
            (query.captured as CfgFolderQuery).name shouldBe folderName
        }

        "retrieveObjectiveTable should create the proper query " {
            val objectiveTableName = "objectiveTable"
            service.retrieveObjectiveTable(objectiveTableName) shouldBe null
            type.captured shouldBe CfgObjectiveTable::class.java
            query.captured.javaClass shouldBe CfgObjectiveTableQuery::class.java
            (query.captured as CfgObjectiveTableQuery).name shouldBe objectiveTableName
        }

        "retrievePerson should create the proper query " {
            val employeeId = "employeeId"
            service.retrievePerson(employeeId) shouldBe null
            type.captured shouldBe CfgPerson::class.java
            query.captured.javaClass shouldBe CfgPersonQuery::class.java
            (query.captured as CfgPersonQuery).employeeId shouldBe employeeId
        }

        "retrievePlace should create the proper query " {
            val placeName = "place"
            service.retrievePlace(placeName) shouldBe null
            type.captured shouldBe CfgPlace::class.java
            query.captured.javaClass shouldBe CfgPlaceQuery::class.java
            (query.captured as CfgPlaceQuery).name shouldBe placeName
        }

        "retrieveScript should create the proper query " {
            val scriptName = "script"
            service.retrieveScript(scriptName) shouldBe null
            type.captured shouldBe CfgScript::class.java
            query.captured.javaClass shouldBe CfgScriptQuery::class.java
            (query.captured as CfgScriptQuery).name shouldBe scriptName
        }

        "retrieveSkill should create the proper query " {
            val skillName = "skill"
            service.retrieveSkill(skillName) shouldBe null
            type.captured shouldBe CfgSkill::class.java
            query.captured.javaClass shouldBe CfgSkillQuery::class.java
            (query.captured as CfgSkillQuery).name shouldBe skillName
        }

        "retrieveSwitch should create the proper query " {
            val switchName = "switch"
            service.retrieveSwitch(switchName) shouldBe null
            type.captured shouldBe CfgSwitch::class.java
            query.captured.javaClass shouldBe CfgSwitchQuery::class.java
            (query.captured as CfgSwitchQuery).name shouldBe switchName
        }
    }
}
