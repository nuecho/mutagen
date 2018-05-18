package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.applicationblocks.com.objects.CfgSubcode
import com.genesyslab.platform.configuration.protocol.types.CfgActionCodeType.CFGTransfer
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgActionCode
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgActionCodeType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every

private const val SUBNAME = "subname"
private const val SUBCODE = "subcode"
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

class ActionCodeTest : ConfigurationObjectTest(actionCode, ActionCode("name"), ActionCode(mockCfgActionCode())) {
    init {
        val service = mockConfService()

        "ActionCode.updateCfgObject should properly create CfgActionCode" {
            every { service.retrieveObject(CfgActionCode::class.java, any()) } returns null

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
