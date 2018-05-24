package com.nuecho.genesys.cli.models.configuration.reference

import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGActionCode
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGDN
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGEnumerator
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

class ConfigurationObjectReferenceTest : StringSpec() {
    init {
        "ConfigurationObjectReference should be sort properly" {
            val reference1 = ActionCodeReference("2-action_code", DEFAULT_TENANT_REFERENCE)
            val reference2 = ActionCodeReference("3-action_code", DEFAULT_TENANT_REFERENCE)
            val reference3 = DNReference(
                switch = "switch1",
                number = "111",
                type = CfgDNType.CFGACDQueue,
                name = "aaa"
            )
            val reference4 = PhysicalSwitchReference("1-phys-switch")

            val list = listOf(reference4, reference3, reference2, reference1).sorted()

            list shouldBe listOf(reference1, reference2, reference3, reference4)
        }

        "ConfigurationObjectReference.getCfgObjectType should return the correct type" {
            ActionCodeReference("action_code", DEFAULT_TENANT_REFERENCE).getCfgObjectType() shouldBe CFGActionCode
            EnumeratorReference("enumerator", DEFAULT_TENANT_REFERENCE).getCfgObjectType() shouldBe CFGEnumerator
            DNReference(
                switch = "switch1",
                number = "111",
                type = CfgDNType.CFGACDQueue,
                name = "aaa"
            ).getCfgObjectType() shouldBe CFGDN
        }
    }
}
