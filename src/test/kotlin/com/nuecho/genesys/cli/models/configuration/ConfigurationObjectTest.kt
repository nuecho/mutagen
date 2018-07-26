package com.nuecho.genesys.cli.models.configuration

import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.services.ServiceMocks
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import io.mockk.every
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
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

    private val configurationObjectType = configurationObject::class.simpleName!!.toLowerCase()

    @Test
    fun `empty object missing mandatory properties should throw MandatoryPropertiesNotSetException`() {
        val service = ServiceMocks.mockConfService()
        every { service.retrieveObject(any(), any()) } returns null

        assertThat(emptyConfigurationObject.checkMandatoryProperties(service), equalTo(mandatoryProperties))
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

    @Test
    fun `cloneBare() should return a clone that contains all mandatory properties`() {
        val bare = configurationObject.cloneBare()
        bare?.let {
            assertThat(bare.checkMandatoryProperties(mockConfService()), empty())
        }
    }
}
