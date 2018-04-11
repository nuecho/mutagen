package com.nuecho.genesys.cli.models.configuration

import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkUserProperties
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.services.ConfService
import io.kotlintest.specs.StringSpec

@Suppress("UnnecessaryAbstractClass")
abstract class ConfigurationObjectTest(
    configurationObject: ConfigurationObject,
    emptyConfigurationObject: ConfigurationObject
) : StringSpec() {
    val service = ConfService(Environment(host = "test", user = "test", rawPassword = "test"))

    init {
        val configurationObjectType = configurationObject::class.simpleName!!.toLowerCase()

        "empty $configurationObjectType should properly serialize" {
            checkSerialization(emptyConfigurationObject, "empty_$configurationObjectType")
        }

        "fully initialized $configurationObjectType should properly serialize" {
            checkSerialization(configurationObject, "$configurationObjectType")
        }

        "$configurationObjectType should properly deserialize" {
            val deserializedConfigurationObject = loadJsonConfiguration(
                "models/configuration/$configurationObjectType.json",
                configurationObject::class.java
            )

            // Normally we should simply check that 'deserializedConfigurationObject shouldBe configurationObject' but
            // since ConfigurationObjectWithUserProperties.equals is broken because of ByteArray.equals, this should do
            // the trick for now.
            checkSerialization(deserializedConfigurationObject, "$configurationObjectType")
            checkUserProperties(configurationObject.userProperties!!, deserializedConfigurationObject.userProperties!!)
        }
    }
}
