package com.nuecho.genesys.cli.models.configuration.reference

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.queries.CfgObjectiveTableQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPersonQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPlaceQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgScriptQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgSkillQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgSwitchQuery
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
class SimpleObjectReferenceTest {
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

    val service = mockConfService()

    @BeforeAll
    fun init() {
        mockRetrieveTenant(service)
    }

    @Test
    fun `SimpleObjectReference should be serialized as a JSON String`() {
        checkSerialization(container, "reference/simple_object_reference")
    }

    @Test
    fun `String reference should be deserialized as a SimpleObjectReference`() {
        val deserializedContainer = loadJsonConfiguration(
            "models/configuration/reference/simple_object_reference.json",
            Container::class.java
        )

        assertThat(deserializedContainer.tenant, equalTo(container.tenant))
        for (i in 0 until container.listOfPerson.size) {
            assertThat(deserializedContainer.listOfPerson[i].tenant, `is`(nullValue()))
            assertThat(deserializedContainer.listOfPerson[i].primaryKey, equalTo(container.listOfPerson[i].primaryKey))
        }

        container.mapOfSkill.forEach {
            assertThat(deserializedContainer.mapOfSkill[SkillReference(it.key.primaryKey, null)], equalTo(it.value))
        }
    }

    @Test
    fun `SimpleObjectReference toString() should generate the proper String`() {
        assertThat(TenantReference(DEFAULT_TENANT).toString(), equalTo(DEFAULT_TENANT))
    }

    @Test
    fun `SimpleObjectReferenceWithTenant toString() should generate the proper String`() {
        val employeeId = "employeeId"
        assertThat(PersonReference(employeeId, DEFAULT_TENANT_REFERENCE).toString(), equalTo("$DEFAULT_TENANT/$employeeId"))
    }

    @Test
    fun `ObjectiveTableReference toQuery should create the proper query`() {
        val objectiveTableName = "objectiveTable"
        val actual = ObjectiveTableReference(objectiveTableName, DEFAULT_TENANT_REFERENCE).toQuery(service)
        assertThat(actual.javaClass, equalTo(CfgObjectiveTableQuery::class.java))
        assertThat(actual.name, equalTo(objectiveTableName))
    }

    @Test
    fun `PersonReference toQuery should create the proper query`() {
        val employeeId = "employeeId"
        val actual = PersonReference(employeeId, DEFAULT_TENANT_REFERENCE).toQuery(service)
        assertThat(actual.javaClass, equalTo(CfgPersonQuery::class.java))
        assertThat(actual.employeeId, equalTo(employeeId))
    }

    @Test
    fun `PlaceReference toQuery should create the proper query`() {
        val placeName = "place"
        val actual = PlaceReference(placeName, DEFAULT_TENANT_REFERENCE).toQuery(service)
        assertThat(actual.javaClass, equalTo(CfgPlaceQuery::class.java))
        assertThat(actual.name, equalTo(placeName))
    }

    @Test
    fun `ScriptReference toQuery should create the proper query`() {
        val scriptName = "script"
        val actual = ScriptReference(scriptName, DEFAULT_TENANT_REFERENCE).toQuery(service)
        assertThat(actual.javaClass, equalTo(CfgScriptQuery::class.java))
        assertThat(actual.name, equalTo(scriptName))
    }

    @Test
    fun `SkillReference toQuery should create the proper query`() {
        val skillName = "skill"
        val actual = SkillReference(skillName, DEFAULT_TENANT_REFERENCE).toQuery(service)
        assertThat(actual.javaClass, equalTo(CfgSkillQuery::class.java))
        assertThat(actual.name, equalTo(skillName))
    }

    @Test
    fun `SwitchReference toQuery should create the proper query`() {
        val switchName = "switch"
        val actual = SwitchReference(switchName, DEFAULT_TENANT_REFERENCE).toQuery(service)
        assertThat(actual.javaClass, equalTo(CfgSwitchQuery::class.java))
        assertThat(actual.name, equalTo(switchName))
    }
}
