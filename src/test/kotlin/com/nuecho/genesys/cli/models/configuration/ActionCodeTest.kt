package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.configuration.protocol.types.CfgActionCodeType.CFGTransfer
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.SUBCODE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.SUBNAME
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgActionCode
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgSubcode
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgActionCodeType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val NAME = "name"
private val TYPE = CFGTransfer.toShortName()
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
    @Test
    fun `updateCfgObject should properly create CfgActionCode`() {
        val service = mockConfService()
        every { service.retrieveObject(CfgActionCode::class.java, any()) } returns null
        mockRetrieveTenant(service)

        val cfgActionCode = actionCode.updateCfgObject(service)

        with(cfgActionCode) {
            assertEquals(actionCode.name, name)
            assertEquals(toCfgActionCodeType(actionCode.type), type)
            assertEquals(actionCode.code, code)
            assertEquals(1, subcodes.size)

            with(subcodes.iterator().next()) {
                assertEquals(SUBNAME, name)
                assertEquals(SUBCODE, code)
            }

            assertEquals(toCfgObjectState(actionCode.state), state)
            assertEquals(actionCode.userProperties, userProperties.asCategorizedProperties())
        }
    }
}

private fun mockCfgActionCode(): CfgActionCode {
    val service = mockConfService()
    val cfgActionCode = mockCfgActionCode(actionCode.name)
    val subcode = mockCfgSubcode()

    return cfgActionCode.apply {
        every { type } returns toCfgActionCodeType(actionCode.type)
        every { code } returns actionCode.code
        every { subcodes } returns listOf(subcode)
        every { state } returns toCfgObjectState(actionCode.state)
        every { userProperties } returns mockKeyValueCollection()
        every { configurationService } returns service
    }
}
