package com.nuecho.genesys.cli.models.configuration.reference

import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.nuecho.genesys.cli.TestResources.loadJsonConfiguration
import com.nuecho.genesys.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_REFERENCE
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.genesys.cli.services.ServiceMocks
import com.nuecho.genesys.cli.services.getObjectDbid
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val DBID = 102
private const val ENUMERATOR = "enumerator"
private val ENUMERATOR_REFERENCE = EnumeratorReference(ENUMERATOR, DEFAULT_TENANT_REFERENCE)
private const val NAME = "enumeratorValue"

class EnumeratorValueReferenceTest {
    private val enumeratorValueReference = EnumeratorValueReference(
        name = NAME,
        enumerator = ENUMERATOR_REFERENCE
    )

    @Test
    fun `EnumeratorValueReference should be serialized as a JSON Object without tenant references`() {
        checkSerialization(enumeratorValueReference, "reference/enumerator_value_reference")
    }

    @Test
    fun `EnumeratorValueReference should properly deserialize`() {
        val deserializedEnumeratorValueReference = loadJsonConfiguration(
            "models/configuration/reference/enumerator_value_reference.json",
            EnumeratorValueReference::class.java
        )

        assertEquals(deserializedEnumeratorValueReference.enumerator, EnumeratorReference(ENUMERATOR, null))
        assertEquals(deserializedEnumeratorValueReference.name, NAME)
    }

    @Test
    fun `EnumeratorValueReference toQuery should create the proper query`() {
        val cfgTenant = mockCfgTenant(DEFAULT_TENANT)

        val service = ServiceMocks.mockConfService()
        every { service.retrieveObject(CfgTenant::class.java, any()) } returns cfgTenant
        every { service.getObjectDbid(ENUMERATOR_REFERENCE) } returns DBID
        every { service.retrieveObject(ENUMERATOR_REFERENCE.cfgObjectClass, any()) } answers { CfgEnumerator(service).apply { dbid = DBID } }

        val query = enumeratorValueReference.toQuery(service)
        assertEquals(query.enumeratorDbid, DBID)
        assertEquals(query.name, NAME)
    }

    @Test
    fun `EnumeratorValueReference toString() should generate the proper String`() {
        assertEquals(enumeratorValueReference.toString(), "name: '$NAME', enumerator: '$ENUMERATOR_REFERENCE'")
    }

    @Test
    fun `EnumeratorValueReference should be sorted properly`() {
        val enumeratorValueReference1 = EnumeratorValueReference(
            enumerator = EnumeratorReference(NAME, TenantReference("aaaTenant")),
            name = "aaa"
        )

        val enumeratorValueReference2 = EnumeratorValueReference(
            enumerator = EnumeratorReference(NAME, DEFAULT_TENANT_REFERENCE),
            name = "aaa"
        )
        val enumeratorValueReference3 = EnumeratorValueReference(
            enumerator = EnumeratorReference("${NAME}aaa", DEFAULT_TENANT_REFERENCE),
            name = "aaa"
        )

        val enumeratorValueReference4 = EnumeratorValueReference(
            enumerator = EnumeratorReference("${NAME}aaa", DEFAULT_TENANT_REFERENCE),
            name = "ccc"
        )

        val enumeratorValueReference5 = EnumeratorValueReference(
            enumerator = EnumeratorReference("${NAME}bbb", DEFAULT_TENANT_REFERENCE),
            name = "ccc"
        )

        val sortedList = listOf(
            enumeratorValueReference5,
            enumeratorValueReference4,
            enumeratorValueReference3,
            enumeratorValueReference2,
            enumeratorValueReference1
        ).sorted()
        assertEquals(
            sortedList, listOf(
                enumeratorValueReference1,
                enumeratorValueReference2,
                enumeratorValueReference3,
                enumeratorValueReference4,
                enumeratorValueReference5
            )
        )
    }
}
