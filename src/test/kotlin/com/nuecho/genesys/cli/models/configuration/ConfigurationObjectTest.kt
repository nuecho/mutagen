package com.nuecho.genesys.cli.models.configuration

import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkUserProperties
import io.kotlintest.specs.StringSpec

@Suppress("UnnecessaryAbstractClass")
abstract class ConfigurationObjectTest(
    configurationObject: ConfigurationObject,
    emptyConfigurationObject: ConfigurationObject,
    importedConfigurationObject: ConfigurationObject?
) : StringSpec() {

    constructor(configurationObject: ConfigurationObject, emptyConfigurationObject: ConfigurationObject) :
            this(configurationObject, emptyConfigurationObject, null)

    init {
        val configurationObjectType = configurationObject::class.simpleName!!.toLowerCase()

        "empty $configurationObjectType should properly serialize" {
            checkSerialization(emptyConfigurationObject, "empty_$configurationObjectType")
        }

        importedConfigurationObject?.let {
            "CfgObject initialized $configurationObjectType should properly serialize" {
                checkSerialization(importedConfigurationObject, configurationObjectType)
            }
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
