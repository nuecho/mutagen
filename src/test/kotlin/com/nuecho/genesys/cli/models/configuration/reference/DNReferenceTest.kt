package com.nuecho.genesys.cli.models.configuration.reference

import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.applicationblocks.com.queries.CfgDNQuery
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
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
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.specs.StringSpec
import io.mockk.every

class DNReferenceTest : StringSpec() {
    val service = mockConfService()

    data class Container(
        val dn: DNReference,
        val dns: List<DNReference>
    )

    val container = Container(
        dn = DNReference(
            tenant = DEFAULT_TENANT_REFERENCE,
            switch = "switch1",
            number = "123",
            type = CfgDNType.CFGNoDN
        ),
        dns = listOf(
            DNReference(
                tenant = DEFAULT_TENANT_REFERENCE,
                switch = "switch1",
                number = "456",
                type = CfgDNType.CFGACDQueue
            ),
            DNReference(
                tenant = DEFAULT_TENANT_REFERENCE,
                switch = "switch1",
                number = "789",
                type = CfgDNType.CFGCellular,
                name = "dn-789"
            )
        )
    )

    init {
        "DNReference should be serialized as a JSON Object without tenant references" {
            checkSerialization(container, "reference/dn")
        }

        "DNReference should properly deserialize" {
            fun compareDNReferences(actual: DNReference, expected: DNReference) {
                actual.tenant shouldBe null
                actual.switch.tenant shouldBe null
                actual.switch.primaryKey shouldBe expected.switch.primaryKey
                actual.number shouldBe expected.number
                actual.type shouldBe expected.type
                actual.name shouldBe expected.name
            }

            val deserializedContainer = loadJsonConfiguration(
                "models/configuration/reference/dn.json",
                Container::class.java
            )

            compareDNReferences(deserializedContainer.dn, container.dn)
            compareDNReferences(deserializedContainer.dns[0], container.dns[0])
            compareDNReferences(deserializedContainer.dns[1], container.dns[1])
        }

        "DNReference should create the proper query" {
            val switchName = "switch1"
            val number = "789"
            val type = CfgDNType.CFGCellular
            val name = "dn-789"

            val cfgSwitch = mockCfgSwitch(switchName)
            val cfgTenant = mockCfgTenant(DEFAULT_TENANT)

            every { service.retrieveObject(CfgSwitch::class.java, any()) } returns cfgSwitch
            every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant

            val actual = DNReference(
                tenant = DEFAULT_TENANT_REFERENCE,
                switch = switchName,
                number = number,
                type = type,
                name = name
            ).toQuery(service)

            actual shouldNotBe null
            actual!!

            actual.javaClass shouldBe CfgDNQuery::class.java
            actual as CfgDNQuery

            actual.tenantDbid shouldBe DEFAULT_TENANT_DBID
            actual.switchDbid shouldBe DEFAULT_OBJECT_DBID
            actual.dnNumber shouldBe number
            actual.dnType shouldBe type
            actual.name shouldBe name
        }

        "DNReference with name=null should create the proper query" {
            val switchName = "switch1"
            val number = "789"
            val type = CfgDNType.CFGCellular

            val cfgSwitch = mockCfgSwitch(switchName)
            val cfgTenant = mockCfgTenant(DEFAULT_TENANT)

            every { service.retrieveObject(CfgSwitch::class.java, any()) } returns cfgSwitch
            every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant

            val actual = DNReference(
                tenant = DEFAULT_TENANT_REFERENCE,
                switch = switchName,
                number = number,
                type = type
            ).toQuery(service)

            actual shouldNotBe null
            actual!!

            actual.javaClass shouldBe CfgDNQuery::class.java
            actual as CfgDNQuery

            actual.tenantDbid shouldBe DEFAULT_TENANT_DBID
            actual.switchDbid shouldBe DEFAULT_OBJECT_DBID
            actual.dnNumber shouldBe number
            actual.dnType shouldBe type
            actual.name shouldBe null
        }

        "DNReference.toString should work with and without name" {
            DNReference(
                tenant = DEFAULT_TENANT_REFERENCE,
                switch = "switch1",
                number = "789",
                type = CfgDNType.CFGCellular,
                name = "dn-789"
            ).toString() shouldBe "tenant: 'tenant', number: '789', switch: 'switch1', type: '${CfgDNType.CFGCellular.toShortName()}', name: 'dn-789'"

            DNReference(
                tenant = DEFAULT_TENANT_REFERENCE,
                switch = "switch1",
                number = "789",
                type = CfgDNType.CFGCellular
            ).toString() shouldBe "tenant: 'tenant', number: '789', switch: 'switch1', type: '${CfgDNType.CFGCellular.toShortName()}'"
        }

        "DNReference should be sorted properly" {
            val dn1 = DNReference(
                tenant = DEFAULT_TENANT_REFERENCE,
                switch = "switch1",
                number = "111",
                type = CfgDNType.CFGACDQueue,
                name = "aaa"
            )
            val dn2 = DNReference(
                tenant = DEFAULT_TENANT_REFERENCE,
                switch = "switch2",
                number = "111",
                type = CfgDNType.CFGACDQueue,
                name = "aaa"
            )
            val dn3 = DNReference(
                tenant = DEFAULT_TENANT_REFERENCE,
                switch = "switch2",
                number = "222",
                type = CfgDNType.CFGACDQueue,
                name = "aaa"
            )
            val dn4 = DNReference(
                tenant = DEFAULT_TENANT_REFERENCE,
                switch = "switch2",
                number = "222",
                type = CfgDNType.CFGChat
            )
            val dn5 = DNReference(
                tenant = DEFAULT_TENANT_REFERENCE,
                switch = "switch2",
                number = "222",
                type = CfgDNType.CFGChat,
                name = "aaa"
            )
            val dn6 = DNReference(
                tenant = DEFAULT_TENANT_REFERENCE,
                switch = "switch2",
                number = "222",
                type = CfgDNType.CFGChat,
                name = "bbb"
            )

            val list = listOf(dn6, dn5, dn4, dn3, dn2, dn1).sorted()

            list shouldBe listOf(dn1, dn2, dn3, dn4, dn5, dn6)
        }
    }
}
