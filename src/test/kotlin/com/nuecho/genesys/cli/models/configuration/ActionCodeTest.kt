package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.applicationblocks.com.objects.CfgSubcode
import com.genesyslab.platform.configuration.protocol.types.CfgActionCodeType.CFGTransfer
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkUserProperties
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgActionCodeType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.retrieveActionCode
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.staticMockk
import io.mockk.use

private const val SUBNAME = "subname"
private const val SUBCODE = "subcode"

class ActionCodeTest : StringSpec() {
    private val service = ConfService(Environment(host = "test", user = "test", rawPassword = "test"))

    private val actionCode = ActionCode(
        name = "name",
        type = CFGTransfer.toShortName(),
        code = "code",
        subcodes = mapOf(
            SUBNAME to SUBCODE
        ),
        state = CFGEnabled.toShortName(),
        userProperties = defaultProperties()
    )

    init {
        "empty ActionCode should properly serialize" {
            checkSerialization(Role("name"), "empty_action_code")
        }

        "fully initialized ActionCode should properly serialize" {
            checkSerialization(actionCode, "action_code")
        }

        "ActionCode should properly deserialize" {
            val deserializedActionCode = loadJsonConfiguration(
                "models/configuration/action_code.json",
                ActionCode::class.java
            )

            // Normally we should simply check that 'deserialized shouldBe actionCode' but since Role.equals is broken
            // because of ByteArray.equals, this should do the trick for now.
            checkSerialization(deserializedActionCode, "action_code")
            checkUserProperties(actionCode.userProperties!!, deserializedActionCode.userProperties!!)
        }

        "CfgActionCode initialized ActionCode should properly serialize" {
            val actionCode = ActionCode(mockCfgActionCode())
            checkSerialization(actionCode, "action_code")
        }

        "ActionCode.updateCfgObject should properly create CfgActionCode" {
            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
                every { service.retrieveActionCode(any()) } returns null

                val type = toCfgActionCodeType(actionCode.type)
                val state = toCfgObjectState(actionCode.state)
                val (status, cfgObject) = actionCode.updateCfgObject(service)
                val cfgActionCode = cfgObject as CfgActionCode

                status shouldBe ConfigurationObjectUpdateStatus.CREATED

                with(cfgActionCode) {
                    name shouldBe actionCode.name
                    type shouldBe type
                    code shouldBe actionCode.code
                    subcodes.size shouldBe 1

                    with(subcodes.iterator().next()) {
                        name shouldBe SUBNAME
                        code shouldBe SUBCODE
                    }

                    state shouldBe state
                    userProperties.size shouldBe 4
                }
            }
        }
    }

    private fun mockCfgActionCode(): CfgActionCode {
        val cfgActionCode = mockk<CfgActionCode>()

        val subcode = CfgSubcode(service, cfgActionCode)
        subcode.name = SUBNAME
        subcode.code = SUBCODE

        val subcodes = listOf(subcode)
        val type = toCfgActionCodeType(actionCode.type)
        val state = toCfgObjectState(actionCode.state)

        every { cfgActionCode.name } returns actionCode.name
        every { cfgActionCode.type } returns type
        every { cfgActionCode.code } returns actionCode.code
        every { cfgActionCode.subcodes } returns subcodes
        every { cfgActionCode.state } returns state
        every { cfgActionCode.userProperties } returns mockKeyValueCollection()
        every { cfgActionCode.configurationService } returns service

        return cfgActionCode
    }
}
