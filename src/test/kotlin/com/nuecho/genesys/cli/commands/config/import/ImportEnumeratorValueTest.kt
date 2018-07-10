package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumeratorValue
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGEnumerator
import com.nuecho.genesys.cli.models.ImportPlanOperation
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgEnumerator
import com.nuecho.genesys.cli.models.configuration.EnumeratorValue
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.objectMockk
import io.mockk.staticMockk
import io.mockk.use
import io.mockk.verify
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

class ImportEnumeratorValueTest {
    val enumeratorValue = EnumeratorValue(
        default = false,
        displayName = "displayName",
        enumerator = "enumerator",
        name = "name",
        tenant = ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
    )

    fun testImportConfigurationObject(create: Boolean) {
        var retrieveEnumeratorValueResult: CfgEnumeratorValue? = null

        if (!create) {
            val service = mockConfService()

            retrieveEnumeratorValueResult = CfgEnumeratorValue(service).apply {
                enumeratorDBID = DEFAULT_OBJECT_DBID
                name = "name"
            }
        }

        val enumerator = mockCfgEnumerator("enumerator")

        objectMockk(ImportPlanOperation.Companion).use {
            staticMockk("com.nuecho.genesys.cli.services.ConfServiceExtensionsKt").use {
                val service = mockConfService()
                every { service.retrieveObject(CfgEnumeratorValue::class.java, any()) } returns retrieveEnumeratorValueResult
                every { service.retrieveObject(CFGEnumerator, any()) } returns enumerator
                every { service.getObjectDbid(any()) } returns DEFAULT_OBJECT_DBID
                every { ImportPlanOperation.save(any()) } just Runs

                val hasImportedObject = ImportPlanOperation(service, enumeratorValue).apply()
                assertThat(hasImportedObject, `is`(true))
                verify(exactly = 1) { ImportPlanOperation.save(ofType(CfgEnumeratorValue::class)) }
            }
        }
    }

    @Test
    fun `importing an existing EnumeratorValue should try to save it`() {
        testImportConfigurationObject(false)
    }

    @Test
    fun `importing a new EnumeratorValue should try to save it`() {
        testImportConfigurationObject(true)
    }
}
