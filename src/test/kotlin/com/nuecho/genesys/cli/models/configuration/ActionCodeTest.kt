package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.applicationblocks.com.objects.CfgSubcode
import com.genesyslab.platform.configuration.protocol.types.CfgActionCodeType.CFGTransfer
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgActionCode
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgActionCodeType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every

private const val NAME = "name"
private val TYPE = CFGTransfer.toShortName()
private const val SUBNAME = "subname"
private const val SUBCODE = "subcode"
private val actionCode = ActionCode(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    type = TYPE,
    code = "code",
    subcodes = mapOf(
        SUBNAME to SUBCODE
    ),
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties()
)

class ActionCodeTest : ConfigurationObjectTest(
    actionCode,
    ActionCode(tenant = DEFAULT_TENANT_REFERENCE, name = NAME, type = TYPE),
    ActionCode(mockCfgActionCode())
) {
    init {
        "ActionCode.updateCfgObject should properly create CfgActionCode" {
            val service = mockConfService()
            every { service.retrieveObject(CfgActionCode::class.java, any()) } returns null
            mockRetrieveTenant(service)

            val type = toCfgActionCodeType(actionCode.type)
            val state = toCfgObjectState(actionCode.state)
            val cfgActionCode = actionCode.updateCfgObject(service)

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
                userProperties.asCategorizedProperties() shouldBe actionCode.userProperties
            }
        }
    }
}

private fun mockCfgActionCode(): CfgActionCode {
    val service = mockConfService()
    val cfgActionCode = mockCfgActionCode(actionCode.name)
    val subcode = CfgSubcode(service, cfgActionCode).apply {
        name = SUBNAME
        code = SUBCODE
    }

    return cfgActionCode.apply {
        every { type } returns toCfgActionCodeType(actionCode.type)
        every { code } returns actionCode.code
        every { subcodes } returns listOf(subcode)
        every { state } returns toCfgObjectState(actionCode.state)
        every { userProperties } returns mockKeyValueCollection()
        every { configurationService } returns service
    }
}
