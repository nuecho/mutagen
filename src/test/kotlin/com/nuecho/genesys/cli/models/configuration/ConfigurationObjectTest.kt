package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgAppPrototype
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGTenant
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_TYPE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_NAME
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.OwnerReference
import com.nuecho.genesys.cli.services.ServiceMocks
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.staticMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private val EMPTY_CONFIGURATION = Configuration(Metadata(formatName = "JSON", formatVersion = "1.0.0"))
private val OTHER_FOLDER_REFERENCE = FolderReference(
    DEFAULT_FOLDER_TYPE.toShortName(),
    OwnerReference(CFGTenant.toShortName(), DEFAULT_TENANT_NAME),
    listOf("otherFolder")
)

@Suppress("UnnecessaryAbstractClass")
abstract class ConfigurationObjectTest(
    val configurationObject: ConfigurationObject,
    val emptyConfigurationObject: ConfigurationObject,
    val mandatoryProperties: Set<String>,
    val importedConfigurationObject: ConfigurationObject?
) {

    constructor(
        configurationObject: ConfigurationObject,
        emptyConfigurationObject: ConfigurationObject,
        mandatoryProperties: Set<String>
    ) : this(configurationObject, emptyConfigurationObject, mandatoryProperties, null)

    private val configurationObjectType = configurationObject::class.simpleName!!.toLowerCase()

    abstract fun `object with different unchangeable properties' values should return the right unchangeable properties`()

    abstract fun `getReferences() should return all object's references`()

    @Test
    fun `empty object missing mandatory properties should return the right missing properties for an object containing only non nullable fields`() {
        assertThat(emptyConfigurationObject.checkMandatoryProperties(EMPTY_CONFIGURATION, mockConfService()), equalTo(mandatoryProperties))
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
        val service = ServiceMocks.mockConfService()
        every { service.retrieveObject(CfgAppPrototype::class.java, any()) } returns null

        val bare = configurationObject.cloneBare()
        bare?.let {
            assertThat(bare.checkMandatoryProperties(EMPTY_CONFIGURATION, service), equalTo(emptySet()))
        }
    }

    internal fun assertUnchangeableProperties(cfgObject: CfgObject, vararg unchangeableProperties: String) {
        staticMockk("com.nuecho.genesys.cli.GenesysExtensionsKt").use {
            every { cfgObject.getFolderReference() } returns OTHER_FOLDER_REFERENCE
            assertThat(configurationObject.checkUnchangeableProperties(cfgObject), equalTo(setOf(*unchangeableProperties)))
        }
    }
}
