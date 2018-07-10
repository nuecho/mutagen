package com.nuecho.genesys.cli.models

import com.genesyslab.platform.applicationblocks.com.objects.CfgAccessGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgRole
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.nuecho.genesys.cli.models.configuration.AccessGroup
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.models.configuration.Role
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import io.mockk.every
import io.mockk.objectMockk
import io.mockk.use
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class ImportPlanOperationTest {

    @Test
    fun `type should be CREATE when remote object does not exist`() {
        val service = mockConfService()

        every { service.retrieveObject(CfgTenant::class.java, any()) } returns null
        every { service.retrieveObject(CfgAccessGroup::class.java, any()) } returns null

        val operation = ImportPlanOperation(
            service,
            AccessGroup(tenant = ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE, name = "name")
        )

        assertThat(operation.type, equalTo(ImportOperationType.CREATE))
    }

    @Test
    fun `type should be UPDATE when remote object exists and is modified`() {
        val service = mockConfService()

        val mockCfgTenant = mockCfgTenant(ConfigurationObjectMocks.DEFAULT_TENANT)

        val remoteCfgObject = CfgRole(service).also {
            it.tenantDBID = ConfigurationObjectMocks.DEFAULT_TENANT_DBID
            it.name = "name"
        }

        val modifiedObject = Role(
            tenant = ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE,
            name = "name",
            description = "updated description"
        )

        objectMockk(ImportPlanOperation).use {

            every { service.retrieveObject(CfgTenant::class.java, any()) } returns mockCfgTenant
            every { service.retrieveObject(CfgRole::class.java, any()) } returns remoteCfgObject
            every { ImportPlanOperation.save(any()) } returns Unit

            val operation = ImportPlanOperation(service, modifiedObject)

            assertThat(operation.type, equalTo(ImportOperationType.UPDATE))
        }
    }

    @Disabled
    @Test
    fun `type should be SKIP when remote object exists and is not modified`() {
        val service = mockConfService()

        val mockCfgTenant = mockCfgTenant(ConfigurationObjectMocks.DEFAULT_TENANT)

        val remoteCfgObject = CfgRole(service).also {
            it.tenantDBID = ConfigurationObjectMocks.DEFAULT_TENANT_DBID
            it.name = "name"
        }

        val unmodifiedObject = Role(
            tenant = ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE,
            name = "name"
        )

        objectMockk(ImportPlanOperation).use {

            every { service.retrieveObject(CfgTenant::class.java, any()) } returns mockCfgTenant
            every { service.retrieveObject(CfgRole::class.java, any()) } returns remoteCfgObject
            every { ImportPlanOperation.save(any()) } returns Unit

            val operation = ImportPlanOperation(service, unmodifiedObject)

            assertThat(operation.type, equalTo(ImportOperationType.SKIP))
        }
    }
}
