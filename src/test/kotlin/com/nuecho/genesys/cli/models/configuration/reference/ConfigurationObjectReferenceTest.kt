package com.nuecho.genesys.cli.models.configuration.reference

import com.genesyslab.platform.configuration.protocol.types.CfgActionCodeType.CFGTransfer
import com.genesyslab.platform.configuration.protocol.types.CfgDNType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGActionCode
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGDN
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGEnumerator
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.toShortName
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

class ConfigurationObjectReferenceTest {
    @Test
    fun `ConfigurationObjectReference should be sorted properly`() {
        val reference1 = ActionCodeReference(
            "2-action_code",
            CFGTransfer.toShortName(),
            DEFAULT_TENANT_REFERENCE
        )

        val reference2 = ActionCodeReference(
            "3-action_code",
            CFGTransfer.toShortName(),
            DEFAULT_TENANT_REFERENCE
        )
        val reference3 = DNReference(
            number = "111",
            switch = "switch1",
            type = CfgDNType.CFGACDQueue,
            name = "aaa",
            tenant = DEFAULT_TENANT_REFERENCE
        )
        val reference4 = PhysicalSwitchReference("1-phys-switch")

        val list = listOf(reference4, reference3, reference2, reference1).sorted()

        assertThat(list, contains(reference1, reference2, reference3, reference4))
    }

    @Test
    fun `getCfgObjectType should return the correct type`() {
        assertThat(
            ActionCodeReference(
                "action_code",
                CFGTransfer.toShortName(),
                DEFAULT_TENANT_REFERENCE
            ).getCfgObjectType(),
            equalTo(CFGActionCode)
        )

        assertThat(EnumeratorReference("enumerator", DEFAULT_TENANT_REFERENCE).getCfgObjectType(), equalTo(CFGEnumerator))

        assertThat(
            DNReference(
                number = "111",
                switch = "switch1",
                type = CfgDNType.CFGACDQueue,
                name = "aaa"
            ).getCfgObjectType(),
            equalTo(CFGDN)
        )
    }
}
