package com.nuecho.genesys.cli.models.configuration.reference

import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.configuration.protocol.types.CfgActionCodeType.CFGBusyOff
import com.genesyslab.platform.configuration.protocol.types.CfgActionCodeType.CFGConference
import com.genesyslab.platform.configuration.protocol.types.CfgActionCodeType.CFGLogin
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_NAME
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.services.ServiceMocks
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test

private const val NAME = "actionCode"
private val TYPE = CFGBusyOff

class ActionCodeReferenceTest {
    private val actionCodeReference = ActionCodeReference(
        name = NAME,
        type = TYPE,
        tenant = DEFAULT_TENANT_REFERENCE
    )

    @Test
    fun `ActionCodeReference should be serialized as a JSON Object without tenant references`() {
        checkSerialization(actionCodeReference, "reference/action_code_reference")
    }

    @Test
    fun `ActionCodeReference should properly deserialize`() {
        val deserializedActionCodeReference = loadJsonConfiguration(
            "models/configuration/reference/action_code_reference.json",
            ActionCodeReference::class.java
        )

        assertThat(deserializedActionCodeReference.tenant, `is`(nullValue()))
        assertThat(deserializedActionCodeReference.type, equalTo(TYPE.toShortName()))
        assertThat(deserializedActionCodeReference.name, equalTo(NAME))
    }

    @Test
    fun `ActionCodeReference toQuery should create the proper query`() {
        val cfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME)

        val service = ServiceMocks.mockConfService()
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant

        val query = actionCodeReference.toQuery(service)
        assertThat(query.tenantDbid, equalTo(ConfigurationObjectMocks.DEFAULT_TENANT_DBID))
        assertThat(query.codeType, equalTo(TYPE))
        assertThat(query.name, equalTo(NAME))
    }

    @Test
    fun `toString should generate the proper string`() {
        assertThat("name: '$NAME', type: '${TYPE.toShortName()}', tenant: '$DEFAULT_TENANT_NAME'", equalTo(actionCodeReference.toString()))
    }

    @Test
    fun `ActionCodeReference should be sorted properly`() {
        val actionCodeReference1 = ActionCodeReference(
            name = "aaa",
            type = CFGConference,
            tenant = TenantReference("aaaTenant")
        )

        val actionCodeReference2 = ActionCodeReference(
            name = "aaa",
            type = CFGConference,
            tenant = DEFAULT_TENANT_REFERENCE
        )

        val actionCodeReference3 = ActionCodeReference(
            name = "aaa",
            type = CFGLogin,
            tenant = DEFAULT_TENANT_REFERENCE
        )

        val actionCodeReference4 = ActionCodeReference(
            name = "bbb",
            type = CFGLogin,
            tenant = DEFAULT_TENANT_REFERENCE
        )

        val actionCodeReference5 = ActionCodeReference(
            name = "ccc",
            type = CFGLogin,
            tenant = DEFAULT_TENANT_REFERENCE
        )

        val sortedList = listOf(actionCodeReference5, actionCodeReference4, actionCodeReference3, actionCodeReference2, actionCodeReference1).sorted()
        assertThat(sortedList, contains(actionCodeReference1, actionCodeReference2, actionCodeReference3, actionCodeReference4, actionCodeReference5))
    }
}
