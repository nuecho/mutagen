package com.nuecho.genesys.cli.models.configuration.reference

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.queries.CfgAgentLoginQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgFolderQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgObjectiveTableQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPersonQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPlaceQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgScriptQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgSkillQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgSwitchQuery
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

class SimpleObjectReferenceTest : StringSpec() {
    val service = mockConfService()

    data class Container(
        val tenant: TenantReference,
        val listOfPerson: List<PersonReference>,
        @JsonDeserialize(keyUsing = SimpleObjectReferenceKeyDeserializer::class)
        @JsonSerialize(keyUsing = SimpleObjectReferenceKeySerializer::class)
        val mapOfSkill: Map<SkillReference, Int>
    )

    val dummy = Container(
        tenant = TenantReference("tenant-name"),
        listOfPerson = listOf(
            PersonReference("employee1"),
            PersonReference("employee2"),
            PersonReference("employee3")
        ),
        mapOfSkill = mapOf(
            SkillReference("skill1") to 1,
            SkillReference("skill2") to 2,
            SkillReference("skill3") to 3
        )
    )

    init {
        "SimpleObjectReference should be serialized as a JSON String" {
            checkSerialization(dummy, "reference/simple-object-reference")
        }

        "String reference should be deserialized as a SimpleObjectReference" {
            val deserializesDummy = loadJsonConfiguration(
                "models/configuration/reference/simple-object-reference.json",
                Container::class.java
            )

            deserializesDummy shouldBe dummy
        }

        "SimpleObjectReference.toString should generate the proper String" {
            val loginCode = "loginCode"
            AgentLoginReference(loginCode).toString() shouldBe "loginCode"
        }

        "AgentLoginReference should create the proper query" {
            val loginCode = "loginCode"
            val actual = AgentLoginReference(loginCode).toQuery(service)
            actual.javaClass shouldBe CfgAgentLoginQuery::class.java
            actual.loginCode shouldBe loginCode
        }

        "FolderReference should create the proper query" {
            val folderName = "folder"
            val actual = FolderReference(folderName).toQuery(service)
            actual.javaClass shouldBe CfgFolderQuery::class.java
            actual.name shouldBe folderName
        }

        "ObjectiveTableReference should create the proper query" {
            val objectiveTableName = "objectiveTable"
            val actual = ObjectiveTableReference(objectiveTableName).toQuery(service)
            actual.javaClass shouldBe CfgObjectiveTableQuery::class.java
            actual.name shouldBe objectiveTableName
        }

        "PersonReference should create the proper query" {
            val employeeId = "employeeId"
            val actual = PersonReference(employeeId).toQuery(service)
            actual.javaClass shouldBe CfgPersonQuery::class.java
            actual.employeeId shouldBe employeeId
        }

        "PlaceReference should create the proper query" {
            val placeName = "place"
            val actual = PlaceReference(placeName).toQuery(service)
            actual.javaClass shouldBe CfgPlaceQuery::class.java
            actual.name shouldBe placeName
        }

        "ScriptReference should create the proper query" {
            val scriptName = "script"
            val actual = ScriptReference(scriptName).toQuery(service)
            actual.javaClass shouldBe CfgScriptQuery::class.java
            actual.name shouldBe scriptName
        }

        "SkillReference should create the proper query" {
            val skillName = "skill"
            val actual = SkillReference(skillName).toQuery(service)
            actual.javaClass shouldBe CfgSkillQuery::class.java
            actual.name shouldBe skillName
        }

        "SwitchReference should create the proper query" {
            val switchName = "switch"
            val actual = SwitchReference(switchName).toQuery(service)
            actual.javaClass shouldBe CfgSwitchQuery::class.java
            actual.name shouldBe switchName
        }
    }
}
