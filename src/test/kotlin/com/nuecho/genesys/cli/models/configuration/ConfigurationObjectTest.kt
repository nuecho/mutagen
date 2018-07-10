package com.nuecho.genesys.cli.models.configuration

import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Suppress("UnnecessaryAbstractClass")
abstract class ConfigurationObjectTest(
    val configurationObject: ConfigurationObject,
    val emptyConfigurationObject: ConfigurationObject,
    val mandatoryProperties: Set<String>,
    val importedConfigurationObject: ConfigurationObject?
) {

    constructor(configurationObject: ConfigurationObject, emptyConfigurationObject: ConfigurationObject, mandatoryProperties: Set<String>) :
            this(configurationObject, emptyConfigurationObject, mandatoryProperties, null)

    val configurationObjectType = configurationObject::class.simpleName!!.toLowerCase()

    @Test
    fun `empty object missing mandatory properties should throw MandatoryPropertiesNotSetException`() {
        assertThat(emptyConfigurationObject.checkMandatoryProperties(), equalTo(mandatoryProperties))
    }

    @Test
    fun `empty object should properly serialize`() {
        checkSerialization(emptyConfigurationObject, "empty_$configurationObjectType")
    }

    @Test
    open fun `initialized object should properly serialize`() {
        checkSerialization(importedConfigurationObject!!, configurationObjectType)
    }

    @Test
    fun `fully initialized object should properly serialize`() {
        checkSerialization(configurationObject, configurationObjectType)
    }

    @Test
    fun `object should properly deserialize`() {
        val deserializedConfigurationObject = loadJsonConfiguration(
            "models/configuration/$configurationObjectType.json",
            configurationObject::class.java
        )

        // Normally we should simply check: `assertThat(deserializedConfigurationObject, equalTo(configurationObject))`, but
        // since ConfigurationObjectWithUserProperties.equals is broken because of ByteArray.equals, this should do
        // the trick for now.
        checkSerialization(deserializedConfigurationObject, configurationObjectType)
    }

    @Disabled
    @Test
    fun `should report object as modified`() {
    }

    @Disabled
    @Test
    fun `should report object as not modified`() {
    }
}
