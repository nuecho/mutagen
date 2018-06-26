package com.nuecho.genesys.cli.models.configuration.reference

import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.configuration.protocol.types.CfgActionCodeType.CFGBusyOff
import com.genesyslab.platform.configuration.protocol.types.CfgActionCodeType.CFGConference
import com.genesyslab.platform.configuration.protocol.types.CfgActionCodeType.CFGLogin
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.services.ServiceMocks
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
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

        assertEquals(deserializedActionCodeReference.tenant, null)
        assertEquals(deserializedActionCodeReference.type, TYPE.toShortName())
        assertEquals(deserializedActionCodeReference.name, NAME)
    }

    @Test
    fun `ActionCodeReference toQuery should create the proper query`() {
        val cfgTenant = mockCfgTenant(DEFAULT_TENANT)

        val service = ServiceMocks.mockConfService()
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant

        val query = actionCodeReference.toQuery(service)
        assertEquals(query.tenantDbid, ConfigurationObjectMocks.DEFAULT_TENANT_DBID)
        assertEquals(query.codeType, TYPE)
        assertEquals(query.name, NAME)
    }

    @Test
    fun `toString `() {
        assertEquals(actionCodeReference.toString(), "name: '$NAME', type: '${TYPE.toShortName()}', tenant: '$DEFAULT_TENANT'")
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
        assertEquals(sortedList, listOf(actionCodeReference1, actionCodeReference2, actionCodeReference3, actionCodeReference4, actionCodeReference5))
    }
}
