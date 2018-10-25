/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nuecho.mutagen.cli.models.configuration.reference

import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGActionCode
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGEnumerator
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGPerson
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGSwitch
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGTenant
import com.nuecho.mutagen.cli.TestResources
import com.nuecho.mutagen.cli.models.configuration.ConfigurationAsserts.checkSerialization
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_NAME
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectNotFoundException
import com.nuecho.mutagen.cli.services.ServiceMocks.mockConfService
import com.nuecho.mutagen.cli.toShortName
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private val OWNER = OwnerReference(type = CFGTenant.toShortName(), name = DEFAULT_TENANT_NAME)
private val PATH = listOf("Objects", "Persons")
private val TYPE = CFGPerson.toShortName()

class FolderReferenceTest {
    private val folderReference = FolderReference(
        owner = OWNER,
        path = PATH,
        type = TYPE
    )

    @Test
    fun `FolderReference should be serialized as a JSON Object`() {
        checkSerialization(folderReference, "reference/folder_reference")
    }

    @Test
    fun `FolderReference should properly deserialize`() {
        val deserializedFolderReference = TestResources.loadJsonConfiguration(
            "models/configuration/reference/folder_reference.json",
            FolderReference::class.java
        )

        assertThat(deserializedFolderReference.owner, equalTo(OWNER))
        assertThat(deserializedFolderReference.path, equalTo(PATH))
        assertThat(deserializedFolderReference.type, equalTo(TYPE))
    }

    @Test
    fun `FolderReference toQuery should throw a ConfigurationObjectNotFoundException`() {
        assertThrows<ConfigurationObjectNotFoundException> {
            folderReference.toQuery(mockConfService())
        }
    }

    @Test
    fun `FolderReference toString`() {
        assertThat(
            folderReference.toString(),
            equalTo("type: '$TYPE', owner: 'tenant/tenant', path: '${PATH.joinToString("/")}'")
        )
    }

    @Test
    fun `FolderReference should be sorted properly`() {
        val folderReference1 = FolderReference(
            owner = OWNER,
            path = listOf("AAA", "BBB"),
            type = CFGActionCode.toShortName()
        )

        val folderReference2 = FolderReference(
            owner = OWNER,
            path = listOf("YYY", "ZZZ"),
            type = CFGActionCode.toShortName()
        )

        val folderReference3 = FolderReference(
            owner = OwnerReference(CFGEnumerator.toShortName(), "enum"),
            path = PATH,
            type = CFGPerson.toShortName()
        )

        val folderReference4 = FolderReference(
            owner = OwnerReference(CFGSwitch.toShortName(), "aswitch"),
            path = PATH,
            type = CFGPerson.toShortName()
        )

        val folderReference5 = FolderReference(
            owner = OwnerReference(CFGSwitch.toShortName(), "zswitch"),
            path = PATH,
            type = CFGPerson.toShortName()
        )

        val sortedList = listOf(folderReference5, folderReference4, folderReference3, folderReference2, folderReference1).sorted()
        assertThat(sortedList, contains(folderReference1, folderReference2, folderReference3, folderReference4, folderReference5))
    }
}
