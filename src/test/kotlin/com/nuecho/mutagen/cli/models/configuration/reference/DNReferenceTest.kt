/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nuecho.mutagen.cli.models.configuration.reference

import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGACDQueue
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGChat
import com.genesyslab.platform.configuration.protocol.types.CfgDNType.CFGNoDN
import com.nuecho.mutagen.cli.TestResources.loadJsonConfiguration
import com.nuecho.mutagen.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_NAME
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.mutagen.cli.services.ServiceMocks.mockConfService
import com.nuecho.mutagen.cli.toShortName
import io.mockk.every
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
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

        assertThat(deserializedDNReference.tenant, `is`(nullValue()))
        assertThat(deserializedDNReference.switch.tenant, `is`(nullValue()))
        assertThat(deserializedDNReference.switch.primaryKey, equalTo(SWITCH))
        assertThat(deserializedDNReference.number, equalTo(NUMBER))
        assertThat(deserializedDNReference.type, equalTo(TYPE.toShortName()))
        assertThat(deserializedDNReference.name, equalTo(NAME))
    }

    @Test
    fun `DNReference toQuery should create the proper query`() {
        val cfgSwitch = mockCfgSwitch(SWITCH)
        val cfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME)

        val service = mockConfService()
        every { service.retrieveObject(CfgSwitch::class.java, any()) } returns cfgSwitch
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant

        val query = dnReference.toQuery(service)
        assertThat(query.tenantDbid, equalTo(DEFAULT_TENANT_DBID))
        assertThat(query.switchDbid, equalTo(DEFAULT_OBJECT_DBID))
        assertThat(query.dnNumber, equalTo(NUMBER))
        assertThat(query.dnType, equalTo(TYPE))
        assertThat(query.name, equalTo(NAME))
    }

    @Test
    fun `DNReference with name=null toQuery should create the proper query`() {
        val cfgSwitch = mockCfgSwitch(dnReference.switch.primaryKey)
        val cfgTenant = mockCfgTenant(DEFAULT_TENANT_NAME)

        val service = mockConfService()
        every { service.retrieveObject(CfgSwitch::class.java, any()) } returns cfgSwitch
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant

        val query = dnReference.copy(name = null).toQuery(service)

        assertThat(query.tenantDbid, equalTo(DEFAULT_TENANT_DBID))
        assertThat(query.switchDbid, equalTo(DEFAULT_OBJECT_DBID))
        assertThat(query.dnNumber, equalTo(NUMBER))
        assertThat(query.dnType, equalTo(TYPE))
        assertThat(query.name, `is`(nullValue()))
    }

    @Test
    fun `toString with name should generate the proper string`() {
        assertThat("number: '$NUMBER', switch: '$SWITCH', type: '${TYPE.toShortName()}', name: '$NAME', tenant: '$DEFAULT_TENANT_NAME'", equalTo(dnReference.toString()))
    }

    @Test
    fun `toString without name should generate the proper string`() {
        assertThat("number: '$NUMBER', switch: '$SWITCH', type: '${TYPE.toShortName()}', tenant: '$DEFAULT_TENANT_NAME'", equalTo(dnReference.copy(name = null).toString()))
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
        assertThat(sortedList, contains(dnReference1, dnReference2, dnReference3, dnReference4, dnReference5, dnReference6))
    }
}
