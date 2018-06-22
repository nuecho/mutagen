package com.nuecho.genesys.cli.models.configuration

import org.junit.jupiter.api.Disabled

@Suppress("UnnecessaryAbstractClass")
abstract class GroupConfigurationObjectTest(
    configurationObject: ConfigurationObject,
    emptyConfigurationObject: ConfigurationObject
) : ConfigurationObjectTest(configurationObject, emptyConfigurationObject) {
    @Disabled
    override fun `initialized object should properly serialize`() {
    }
}
