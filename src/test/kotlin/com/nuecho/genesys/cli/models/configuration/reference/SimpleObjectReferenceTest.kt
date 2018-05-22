package com.nuecho.genesys.cli.models.configuration.reference

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
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
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every

class SimpleObjectReferenceTest : StringSpec() {
    data class Container(
        val tenant: TenantReference,
        val listOfPerson: List<PersonReference>,
        @JsonDeserialize(keyUsing = SimpleObjectReferenceWithTenantKeyDeserializer::class)
        @JsonSerialize(keyUsing = SimpleObjectReferenceKeySerializer::class)
        val mapOfSkill: Map<SkillReference, Int>
    )

    val container = Container(
        tenant = TenantReference("tenant-name"),
        listOfPerson = listOf(
            PersonReference("employee1", DEFAULT_TENANT_REFERENCE),
            PersonReference("employee2", DEFAULT_TENANT_REFERENCE),
            PersonReference("employee3", DEFAULT_TENANT_REFERENCE)
        ),
        mapOfSkill = mapOf(
            SkillReference("skill1", DEFAULT_TENANT_REFERENCE) to 1,
            SkillReference("skill2", DEFAULT_TENANT_REFERENCE) to 2,
            SkillReference("skill3", DEFAULT_TENANT_REFERENCE) to 3
        )
    )

    init {
        val cfgTenant = mockCfgTenant(ConfigurationObjectMocks.DEFAULT_TENANT)
        val service = mockConfService()
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant

        "SimpleObjectReference should be serialized as a JSON String" {
            checkSerialization(container, "reference/simple-object-reference")
        }

        "String reference should be deserialized as a SimpleObjectReference" {
            val deserializedContainer = loadJsonConfiguration(
                "models/configuration/reference/simple-object-reference.json",
                Container::class.java
            )

            deserializedContainer.tenant shouldBe container.tenant
            for (i in 0 until container.listOfPerson.size) {
                deserializedContainer.listOfPerson[i].tenant shouldBe null
                deserializedContainer.listOfPerson[i].primaryKey shouldBe container.listOfPerson[i].primaryKey
            }

            container.mapOfSkill.forEach {
                deserializedContainer.mapOfSkill[SkillReference(it.key.primaryKey, null)] shouldBe it.value
            }
        }

        "SimpleObjectReference.toString should generate the proper String" {
            val loginCode = "loginCode"
            AgentLoginReference(loginCode, DEFAULT_TENANT_REFERENCE).toString() shouldBe "$DEFAULT_TENANT/loginCode"
        }

        "AgentLoginReference should create the proper query" {
            val loginCode = "loginCode"
            val actual = AgentLoginReference(loginCode, DEFAULT_TENANT_REFERENCE).toQuery(service)
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
            val actual = ObjectiveTableReference(objectiveTableName, DEFAULT_TENANT_REFERENCE).toQuery(service)
            actual.javaClass shouldBe CfgObjectiveTableQuery::class.java
            actual.name shouldBe objectiveTableName
        }

        "PersonReference should create the proper query" {
            val employeeId = "employeeId"
            val actual = PersonReference(employeeId, DEFAULT_TENANT_REFERENCE).toQuery(service)
            actual.javaClass shouldBe CfgPersonQuery::class.java
            actual.employeeId shouldBe employeeId
        }

        "PlaceReference should create the proper query" {
            val placeName = "place"
            val actual = PlaceReference(placeName, DEFAULT_TENANT_REFERENCE).toQuery(service)
            actual.javaClass shouldBe CfgPlaceQuery::class.java
            actual.name shouldBe placeName
        }

        "ScriptReference should create the proper query" {
            val scriptName = "script"
            val actual = ScriptReference(scriptName, DEFAULT_TENANT_REFERENCE).toQuery(service)
            actual.javaClass shouldBe CfgScriptQuery::class.java
            actual.name shouldBe scriptName
        }

        "SkillReference should create the proper query" {
            val skillName = "skill"
            val actual = SkillReference(skillName, DEFAULT_TENANT_REFERENCE).toQuery(service)
            actual.javaClass shouldBe CfgSkillQuery::class.java
            actual.name shouldBe skillName
        }

        "SwitchReference should create the proper query" {
            val switchName = "switch"
            val actual = SwitchReference(switchName, DEFAULT_TENANT_REFERENCE).toQuery(service)
            actual.javaClass shouldBe CfgSwitchQuery::class.java
            actual.name shouldBe switchName
        }
    }
}
