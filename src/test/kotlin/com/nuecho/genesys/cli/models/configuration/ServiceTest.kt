package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.objects.CfgSolutionComponent
import com.genesyslab.platform.applicationblocks.com.objects.CfgSolutionComponentDefinition
import com.genesyslab.platform.configuration.protocol.types.CfgAppType.CFGAgentDesktop
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState.CFGEnabled
import com.genesyslab.platform.configuration.protocol.types.CfgSolutionType.CFGSTBranchOffice
import com.genesyslab.platform.configuration.protocol.types.CfgSolutionType.CFGSTDesktopNETServerSolution
import com.genesyslab.platform.configuration.protocol.types.CfgStartupType.CFGSUTAutomatic
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgService
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgAppType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgSolutionType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgStartupType
import com.nuecho.genesys.cli.models.configuration.ConfigurationTestData.defaultProperties
import com.nuecho.genesys.cli.models.configuration.reference.ApplicationReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveApplication
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveFolderByDbid
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockRetrieveTenant
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.toShortName
import io.mockk.every
import io.mockk.mockk
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

private const val STARTUP_PRIORITY = 2

private val service = Service(
    name = "foo",
    assignedTenant = DEFAULT_TENANT_REFERENCE,
    componentDefinitions = listOf(
        SolutionComponentDefinition(
            startupPriority = STARTUP_PRIORITY,
            type = CFGAgentDesktop.toShortName(),
            isOptional = true,
            version = "1234"
        )
    ),
    components = listOf(
        SolutionComponent(
            app = ApplicationReference("foo"),
            startupPriority = STARTUP_PRIORITY,
            isOptional = true
        )
    ),
    scs = ApplicationReference("foo"),
    solutionType = CFGSTBranchOffice.toShortName(),
    startupType = CFGSUTAutomatic.toShortName(),
    version = "1234",
    state = CFGEnabled.toShortName(),
    userProperties = defaultProperties(),
    folder = DEFAULT_FOLDER_REFERENCE
)

class ServiceTest : ConfigurationObjectTest(
    configurationObject = service,
    emptyConfigurationObject = Service("foo"),
    mandatoryProperties = setOf(SOLUTION_TYPE, VERSION),
    importedConfigurationObject = Service(mockService())
) {
    val confService = mockConfService()

    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(service.assignedTenant)
            .add(service.components!!.map { it.app })
            .add(service.scs)
            .add(service.folder)
            .toSet()

        assertThat(service.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() {
        val cfgService = mockCfgService(name = service.name).also {
            every { it.solutionType } returns CFGSTDesktopNETServerSolution
        }

        assertThat(configurationObject.checkUnchangeableProperties(cfgService), equalTo(setOf(SOLUTION_TYPE)))
    }

    @Test
    fun `updateCfgObject should properly create CfgService`() {
        mockRetrieveApplication(confService)
        mockRetrieveTenant(confService)

        objectMockk(ConfigurationObjectRepository).use {
            mockConfigurationObjectRepository()
            val cfgService = service.createCfgObject(confService)

            with(cfgService) {
                assertThat(name, equalTo(service.name))
                assertThat(assignedTenantDBID, equalTo(DEFAULT_TENANT_DBID))
                with(componentDefinitions.toList()[0]) {
                    val expectedComponent = service.componentDefinitions!![0]
                    assertThat(startupPriority, equalTo(expectedComponent.startupPriority))
                    assertThat(type, equalTo(toCfgAppType(expectedComponent.type)))
                    assertThat(isOptional, equalTo(toCfgFlag(expectedComponent.isOptional)))
                    assertThat(version, equalTo(expectedComponent.version))
                }
                with(components.toList()[0]) {
                    val expectedComponent = service.components!![0]
                    assertThat(appDBID, equalTo(DEFAULT_OBJECT_DBID))
                    assertThat(startupPriority, equalTo(expectedComponent.startupPriority))
                    assertThat(isOptional, equalTo(toCfgFlag(expectedComponent.isOptional)))
                }
                assertThat(components.toList(), equalTo(
                    service.components?.map { it.toCfgSolutionComponent(this) } as Collection<CfgSolutionComponent>
                ))
                assertThat(scsdbid, equalTo(DEFAULT_OBJECT_DBID))
                assertThat(solutionType, equalTo(toCfgSolutionType(service.solutionType)))
                assertThat(startupType, equalTo(toCfgStartupType(service.startupType)))
                assertThat(version, equalTo(service.version))
                assertThat(state, equalTo(toCfgObjectState(service.state)))
                assertThat(userProperties.asCategorizedProperties(), equalTo(service.userProperties))
                assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
            }
        }
    }
}

private fun mockService() = mockCfgService(service.name).apply {
    val confService = mockConfService()
    mockRetrieveFolderByDbid(confService)

    val application = ConfigurationObjectMocks.mockCfgApplication("foo")
    val solutionComponentDefinition = mockSolutionComponentDefinition()
    val solutionComponent = mockSolutionComponent()
    val tenant = ConfigurationObjectMocks.mockCfgTenant("tenant")

    every { configurationService } returns confService
    every { assignedTenant } returns tenant
    every { assignedTenantDBID } returns DEFAULT_TENANT_DBID
    every { componentDefinitions } returns listOf(solutionComponentDefinition)
    every { components } returns listOf(solutionComponent)
    every { scs } returns application
    every { scsdbid } returns DEFAULT_OBJECT_DBID
    every { solutionType } returns CFGSTBranchOffice
    every { startupType } returns CFGSUTAutomatic
    every { version } returns service.version
    every { state } returns toCfgObjectState(service.state)
    every { userProperties } returns mockKeyValueCollection()
    every { folderId } returns DEFAULT_OBJECT_DBID
}

private fun mockSolutionComponentDefinition(): CfgSolutionComponentDefinition {
    val cfgSolutionComponentDefinition = mockk<CfgSolutionComponentDefinition>()

    every { cfgSolutionComponentDefinition.startupPriority } returns STARTUP_PRIORITY
    every { cfgSolutionComponentDefinition.isOptional } returns CfgFlag.CFGTrue
    every { cfgSolutionComponentDefinition.type } returns CFGAgentDesktop
    every { cfgSolutionComponentDefinition.version } returns "1234"

    return cfgSolutionComponentDefinition
}

private fun mockSolutionComponent(): CfgSolutionComponent {
    val cfgSolutionComponent = mockk<CfgSolutionComponent>()

    val application = ConfigurationObjectMocks.mockCfgApplication("foo")

    every { cfgSolutionComponent.startupPriority } returns STARTUP_PRIORITY
    every { cfgSolutionComponent.isOptional } returns CfgFlag.CFGTrue
    every { cfgSolutionComponent.app } returns application
    every { cfgSolutionComponent.appDBID } returns DEFAULT_OBJECT_DBID

    return cfgSolutionComponent
}
