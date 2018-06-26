package com.nuecho.genesys.cli.models.configuration.reference

import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import io.mockk.every
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test

private const val LOGIN_CODE = "loginCode"
private const val SWITCH = "switch"
private val SWITCH_REFERENCE = SwitchReference(SWITCH, DEFAULT_TENANT_REFERENCE)

class AgentLoginReferenceTest {
    private val agentLoginReference = AgentLoginReference(
        loginCode = LOGIN_CODE,
        switch = SWITCH_REFERENCE
    )

    @Test
    fun `AgentLoginReference should be serialized as a JSON Object without tenant references`() {
        checkSerialization(agentLoginReference, "reference/agent_login_reference")
    }

    @Test
    fun `AgentLoginReference should properly deserialize`() {
        val deserializedAgentLoginReference = loadJsonConfiguration(
            "models/configuration/reference/agent_login_reference.json",
            AgentLoginReference::class.java
        )

        assertThat(deserializedAgentLoginReference.loginCode, equalTo(LOGIN_CODE))
        assertThat(deserializedAgentLoginReference.switch.primaryKey, equalTo(SWITCH))
        assertThat(deserializedAgentLoginReference.switch.tenant, `is`(nullValue()))
    }

    @Test
    fun `AgentLoginReference toQuery should create the proper query`() {
        val cfgTenant = mockCfgTenant(DEFAULT_TENANT)
        val cfgSwitch = mockCfgSwitch(SWITCH)

        val service = mockConfService()
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant
        every { service.retrieveObject(CfgSwitch::class.java, any()) } returns cfgSwitch

        val query = agentLoginReference.toQuery(service)
        assertThat(query.loginCode, equalTo(LOGIN_CODE))
        assertThat(query.switchDbid, equalTo(DEFAULT_OBJECT_DBID))
    }

    @Test
    fun `toString should generate the proper string`() {
        assertThat("loginCode: '$LOGIN_CODE', switch: '$DEFAULT_TENANT/$SWITCH'", equalTo(agentLoginReference.toString()))
    }

    @Test
    fun `AgentLoginReference should be sorted properly`() {
        val agentLoginReference1 = AgentLoginReference(
            loginCode = "aaa",
            switch = SwitchReference("aSwitch", TenantReference("aTenant"))
        )

        val agentLoginReference2 = AgentLoginReference(
            loginCode = "aaa",
            switch = SwitchReference("bSwitch", TenantReference("aTenant"))
        )

        val agentLoginReference3 = AgentLoginReference(
            loginCode = "aaa",
            switch = SwitchReference("aSwitch", TenantReference("bTenant"))
        )

        val agentLoginReference4 = AgentLoginReference(
            loginCode = "aaa",
            switch = SwitchReference("bSwitch", TenantReference("bTenant"))
        )

        val agentLoginReference5 = AgentLoginReference(
            loginCode = "bbb",
            switch = SwitchReference("bSwitch", TenantReference("bTenant"))
        )

        val sortedList = listOf(agentLoginReference5, agentLoginReference4, agentLoginReference3, agentLoginReference2, agentLoginReference1).sorted()
        assertThat(sortedList, contains(agentLoginReference1, agentLoginReference2, agentLoginReference3, agentLoginReference4, agentLoginReference5))
    }
}
