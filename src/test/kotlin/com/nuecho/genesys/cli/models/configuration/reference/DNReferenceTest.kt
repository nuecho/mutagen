package com.nuecho.genesys.cli.models.configuration.reference

import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.queries.CfgDNQuery
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk

class DNReferenceTest : StringSpec() {
    val service = mockConfService()

    data class Container(
        val dn: DNReference,
        val dns: List<DNReference>
    )

    val dummy = Container(
        dn = DNReference(
            switch = "switch1",
            number = "123",
            type = CfgDNType.CFGNoDN
        ),
        dns = listOf(
            DNReference(
                switch = "switch1",
                number = "456",
                type = CfgDNType.CFGACDQueue
            ),
            DNReference(
                switch = "switch1",
                number = "789",
                type = CfgDNType.CFGCellular,
                name = "dn-789"
            )
        )
    )

    init {
        "DNReference should be serialized as a JSON Object" {
            checkSerialization(dummy, "reference/dn")
        }

        "DNReference should properly deserialize" {
            val deserializedDummy = loadJsonConfiguration(
                "models/configuration/reference/dn.json",
                Container::class.java
            )

            deserializedDummy shouldBe dummy
        }

        "DNReference should create the proper query" {
            val switchDbid = 101
            val number = "789"
            val type = CfgDNType.CFGCellular
            val name = "dn-789"

            val cfgSwitch = mockk<CfgSwitch>().apply {
                every { objectDbid } returns switchDbid
            }

            every { service.retrieveObject(CfgSwitch::class.java, any()) } returns cfgSwitch

            val actual = DNReference(
                switch = "switch1",
                number = number,
                type = type,
                name = name
            ).toQuery(service)

            actual shouldNotBe null
            actual!!

            actual.javaClass shouldBe CfgDNQuery::class.java
            actual as CfgDNQuery

            actual.switchDbid shouldBe switchDbid
            actual.dnNumber shouldBe number
            actual.dnType shouldBe type
            actual.name shouldBe name
        }

        "DNReference with name=null should create the proper query" {
            val switchDbid = 101
            val number = "789"
            val type = CfgDNType.CFGCellular

            val cfgSwitch = mockk<CfgSwitch>().apply {
                every { objectDbid } returns switchDbid
            }

            every { service.retrieveObject(CfgSwitch::class.java, any()) } returns cfgSwitch

            val actual = DNReference(
                switch = "switch1",
                number = number,
                type = type
            ).toQuery(service)

            actual shouldNotBe null
            actual!!

            actual.javaClass shouldBe CfgDNQuery::class.java
            actual as CfgDNQuery

            actual.switchDbid shouldBe switchDbid
            actual.dnNumber shouldBe number
            actual.dnType shouldBe type
            actual.name shouldBe null
        }

        "DNReference with name=null should create the proper query" {
            DNReference(
                switch = "switch1",
                number = "789",
                type = CfgDNType.CFGCellular,
                name = "dn-789"
            ).toString() shouldBe "number: '789', switch: 'switch1', type: '${CfgDNType.CFGCellular.toShortName()}', name: 'dn-789'"

            DNReference(
                switch = "switch1",
                number = "789",
                type = CfgDNType.CFGCellular
            ).toString() shouldBe "number: '789', switch: 'switch1', type: '${CfgDNType.CFGCellular.toShortName()}'"
        }

        "DNReference should be sorted properly" {
            val dn1 = DNReference(
                switch = "switch1",
                number = "111",
                type = CfgDNType.CFGACDQueue,
                name = "aaa"
            )
            val dn2 = DNReference(
                switch = "switch2",
                number = "111",
                type = CfgDNType.CFGACDQueue,
                name = "aaa"
            )
            val dn3 = DNReference(
                switch = "switch2",
                number = "222",
                type = CfgDNType.CFGACDQueue,
                name = "aaa"
            )
            val dn4 = DNReference(
                switch = "switch2",
                number = "222",
                type = CfgDNType.CFGChat
            )
            val dn5 = DNReference(
                switch = "switch2",
                number = "222",
                type = CfgDNType.CFGChat,
                name = "aaa"
            )
            val dn6 = DNReference(
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
