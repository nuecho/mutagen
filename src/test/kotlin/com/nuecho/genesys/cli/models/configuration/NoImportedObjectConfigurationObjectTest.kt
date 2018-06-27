package com.nuecho.genesys.cli.models.configuration

import org.junit.jupiter.api.Disabled

@Suppress("UnnecessaryAbstractClass")
abstract class NoImportedObjectConfigurationObjectTest(
    configurationObject: ConfigurationObject,
    emptyConfigurationObject: ConfigurationObject,
    mandatoryProperties: Set<String>
    ) : ConfigurationObjectTest(configurationObject, emptyConfigurationObject, mandatoryProperties) {
    @Disabled
    override fun `initialized object should properly serialize`() {
    }
}
