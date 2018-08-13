package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlaceGroup
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGFolder
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPlace
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPlace
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgPlaceGroup
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockEmptyCfgGroup
import com.nuecho.genesys.cli.models.configuration.reference.PlaceReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfServiceExtensionMocks.mockConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ConfigurationObjectRepository
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.staticMockk
import io.mockk.use
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val TENANT_DBID = 109
private const val PLACE1_DBID = 110
private const val PLACE2_DBID = 111

private const val NAME = "placeGroup"
private const val PLACE1 = "place1"
private const val PLACE2 = "place2"

private val placeGroup = PlaceGroup(
    group = Group(
        tenant = DEFAULT_TENANT_REFERENCE,
        name = NAME,
        state = "enabled"
    ),
    places = listOf(PlaceReference(PLACE1, DEFAULT_TENANT_REFERENCE), PlaceReference(PLACE2, DEFAULT_TENANT_REFERENCE)),
    folder = DEFAULT_FOLDER_REFERENCE
)

class PlaceGroupTest : ConfigurationObjectTest(
    configurationObject = placeGroup,
    emptyConfigurationObject = PlaceGroup(tenant = DEFAULT_TENANT_REFERENCE, name = NAME),
    mandatoryProperties = emptySet()
) {
    @Test
    override fun `getReferences() should return all object's references`() {
        val expected = referenceSetBuilder()
            .add(placeGroup.group.getReferences())
            .add(placeGroup.places)
            .add(placeGroup.folder)
            .toSet()

        assertThat(placeGroup.getReferences(), equalTo(expected))
    }

    @Test
    override fun `object with different unchangeable properties' values should return the right unchangeable properties`() =
        assertUnchangeableProperties(mockCfgPlaceGroup(mockConfService()), FOLDER)

    @Test
    override fun `initialized object should properly serialize`() {
        val service = mockConfService()
        val folder = mockCfgFolder()
        val place1 = mockCfgPlace(PLACE1)
        val place2 = mockCfgPlace(PLACE2)

        objectMockk(ConfigurationObjects).use {
            every { service.retrieveObject(CFGFolder, any()) } returns folder
            every { service.retrieveObject(CFGPlace, PLACE1_DBID) } returns place1
            every { service.retrieveObject(CFGPlace, PLACE2_DBID) } returns place2

            val placeGroup = PlaceGroup(mockCfgPlaceGroup(service))
            checkSerialization(placeGroup, "placegroup")
        }
    }

    @Test
    fun `createCfgObject should properly create CfgPlaceGroup`() {
        val place1 = mockCfgPlace(PLACE1)
        val place2 = mockCfgPlace(PLACE2)
        val service = mockConfService()

        every {
            service.retrieveObject(CfgPlace::class.java, any())
        } returns place1 andThen place2
        every { service.retrieveObject(CfgPlaceGroup::class.java, any()) } returns null

        staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
            every { service.getObjectDbid(placeGroup.group.tenant) } answers { TENANT_DBID }
            every { service.getObjectDbid(placeGroup.places!![0]) } answers { PLACE1_DBID }
            every { service.getObjectDbid(placeGroup.places!![1]) } answers { PLACE2_DBID }

            objectMockk(ConfigurationObjectRepository).use {
                mockConfigurationObjectRepository()

                val cfgPlaceGroup = placeGroup.createCfgObject(service)

                with(cfgPlaceGroup) {
                    assertThat(placeDBIDs.toList(), equalTo(listOf(PLACE1_DBID, PLACE2_DBID)))
                    assertThat(groupInfo, equalTo(placeGroup.group.toCfgGroup(service, this)))
                    assertThat(folderId, equalTo(DEFAULT_FOLDER_DBID))
                }
            }
        }
    }
}

private fun mockCfgPlaceGroup(service: IConfService): CfgPlaceGroup {
    val cfgPlaceGroup = mockCfgPlaceGroup(placeGroup.group.name)
    val groupInfoMock = mockEmptyCfgGroup(placeGroup.group.name)

    return cfgPlaceGroup.apply {
        every { configurationService } returns service
        every { folderId } returns DEFAULT_FOLDER_DBID
        every { placeDBIDs } returns listOf(PLACE1_DBID, PLACE2_DBID)

        every { groupInfo } returns groupInfoMock
    }
}
