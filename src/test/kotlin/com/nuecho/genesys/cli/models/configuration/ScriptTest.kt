package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgScriptType.CFGBusinessProcess
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgScriptType
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every

private const val NAME = "name"
private val script = Script(
    tenant = DEFAULT_TENANT_REFERENCE,
    name = NAME,
    type = CFGBusinessProcess.toShortName(),
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties()
)

class ScriptTest : ConfigurationObjectTest(
    script,
    Script(tenant = DEFAULT_TENANT_REFERENCE, name = NAME),
    Script(mockCfgScript())
) {
    init {
        "Script.updateCfgObject should properly create CfgScript" {
            val service = mockConfService()
            every { service.retrieveObject(CfgScript::class.java, any()) } returns null
            mockRetrieveTenant(service)

            val cfgScript = script.updateCfgObject(service)

            with(cfgScript) {
                name shouldBe script.name
                index shouldBe script.index
                state shouldBe toCfgObjectState(script.state)
                type shouldBe toCfgScriptType(script.type)
                userProperties.asCategorizedProperties() shouldBe script.userProperties
            }
        }
    }
}

private fun mockCfgScript() = mockCfgScript(script.name).apply {
    every { state } returns CFGEnabled
    every { type } returns toCfgScriptType(script.type)
    every { index } returns script.index
    every { resources } returns null
    every { userProperties } returns mockKeyValueCollection()
}
