package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgScriptType
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.kotlintest.matchers.shouldBe
import io.mockk.every

private val script = Script(
    name = "foo",
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties()
)

class ScriptTest : ConfigurationObjectTest(script, Script("foo"), Script(mockCfgScript())) {
    init {
        val service = mockConfService()

        "Script.updateCfgObject should properly create CfgScript" {
            every { service.retrieveObject(CfgScript::class.java, any()) } returns null

            val (status, cfgObject) = script.updateCfgObject(service)
            val cfgScript = cfgObject as CfgScript

            status shouldBe ConfigurationObjectUpdateStatus.CREATED

            with(cfgScript) {
                name shouldBe script.name
                index shouldBe script.index
                state shouldBe ConfigurationObjects.toCfgObjectState(script.state)
                type shouldBe ConfigurationObjects.toCfgScriptType(script.type)
                userProperties.asCategorizedProperties() shouldBe script.userProperties
            }
        }
    }
}

private fun mockCfgScript() = mockCfgScript(script.name).also {
    every { it.state } returns CFGEnabled
    every { it.type } returns toCfgScriptType(script.type)
    every { it.index } returns script.index
    every { it.resources } returns null
    every { it.userProperties } returns mockKeyValueCollection()
}
