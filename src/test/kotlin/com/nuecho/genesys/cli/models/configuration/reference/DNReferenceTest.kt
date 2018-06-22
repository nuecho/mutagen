package com.nuecho.genesys.cli.models.configuration.reference

import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGACDQueue
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGChat
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGNoDN
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val NAME = "dn-123"
private const val SWITCH = "switch1"
private const val NUMBER = "123"
private val TYPE = CFGNoDN

class DNReferenceTest {
    private val dnReference = DNReference(
        number = NUMBER,
        switch = SWITCH,
        type = TYPE,
        name = NAME,
        tenant = DEFAULT_TENANT_REFERENCE
    )

    @Test
    fun `DNReference should be serialized as a JSON Object without tenant references`() {
        checkSerialization(dnReference, "reference/dn_reference")
    }

    @Test
    fun `DNReference should properly deserialize`() {
        val deserializedDNReference = loadJsonConfiguration(
            "models/configuration/reference/dn_reference.json",
            DNReference::class.java
        )

        assertEquals(deserializedDNReference.tenant, null)
        assertEquals(deserializedDNReference.switch.tenant, null)
        assertEquals(deserializedDNReference.switch.primaryKey, SWITCH)
        assertEquals(deserializedDNReference.number, NUMBER)
        assertEquals(deserializedDNReference.type, TYPE.toShortName())
        assertEquals(deserializedDNReference.name, NAME)
    }

    @Test
    fun `DNReference toQuery should create the proper query`() {
        val cfgSwitch = mockCfgSwitch(SWITCH)
        val cfgTenant = mockCfgTenant(DEFAULT_TENANT)

        val service = mockConfService()
        every { service.retrieveObject(CfgSwitch::class.java, any()) } returns cfgSwitch
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant

        val query = dnReference.toQuery(service)
        assertEquals(query.tenantDbid, DEFAULT_TENANT_DBID)
        assertEquals(query.switchDbid, DEFAULT_OBJECT_DBID)
        assertEquals(query.dnNumber, NUMBER)
        assertEquals(query.dnType, TYPE)
        assertEquals(query.name, NAME)
    }

    @Test
    fun `DNReference with name=null toQuery should create the proper query`() {
        val cfgSwitch = mockCfgSwitch(dnReference.switch.primaryKey)
        val cfgTenant = mockCfgTenant(DEFAULT_TENANT)

        val service = mockConfService()
        every { service.retrieveObject(CfgSwitch::class.java, any()) } returns cfgSwitch
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant

        val query = dnReference.copy(name = null).toQuery(service)

        assertEquals(query.tenantDbid, DEFAULT_TENANT_DBID)
        assertEquals(query.switchDbid, DEFAULT_OBJECT_DBID)
        assertEquals(query.dnNumber, NUMBER)
        assertEquals(query.dnType, TYPE)
        assertEquals(query.name, null)
    }

    @Test
    fun `toString with name`() {
        assertEquals(dnReference.toString(), "number: '$NUMBER', switch: '$SWITCH', type: '${TYPE.toShortName()}', name: '$NAME', tenant: '$DEFAULT_TENANT'")
    }

    @Test
    fun `toString without name`() {
        assertEquals(dnReference.copy(name = null).toString(), "number: '$NUMBER', switch: '$SWITCH', type: '${TYPE.toShortName()}', tenant: '$DEFAULT_TENANT'")
    }

    @Test
    fun `DNReference should be sorted properly`() {
        val dnReference1 = DNReference(
            number = "111",
            switch = "switch1",
            type = CFGACDQueue,
            name = "aaa",
            tenant = TenantReference("aaaTenant")
        )
        val dnReference2 = DNReference(
            number = "111",
            switch = "switch1",
            type = CFGACDQueue,
            name = "aaa",
            tenant = DEFAULT_TENANT_REFERENCE
        )

        val dnReference3 = DNReference(
            number = "111",
            switch = "switch2",
            type = CFGACDQueue,
            name = "aaa",
            tenant = DEFAULT_TENANT_REFERENCE
        )

        val dnReference4 = DNReference(
            number = "222",
            switch = "switch2",
            type = CFGChat,
            tenant = DEFAULT_TENANT_REFERENCE
        )

        val dnReference5 = DNReference(
            number = "222",
            switch = "switch2",
            type = CFGChat,
            name = "aaa",
            tenant = DEFAULT_TENANT_REFERENCE
        )

        val dnReference6 = DNReference(
            number = "222",
            switch = "switch2",
            type = CFGChat,
            name = "bbb",
            tenant = DEFAULT_TENANT_REFERENCE
        )

        val sortedList = listOf(dnReference6, dnReference5, dnReference4, dnReference3, dnReference2, dnReference1).sorted()
        assertEquals(sortedList, listOf(dnReference1, dnReference2, dnReference3, dnReference4, dnReference5, dnReference6))
    }
}
