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

package com.nuecho.mutagen.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction
import com.nuecho.mutagen.cli.getFolderReference
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgTransactionType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.mutagen.cli.models.configuration.reference.FolderReference
import com.nuecho.mutagen.cli.models.configuration.reference.TenantReference
import com.nuecho.mutagen.cli.models.configuration.reference.TransactionReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.services.getObjectDbid
import com.nuecho.mutagen.cli.toShortName

data class Transaction(
    val tenant: TenantReference,
    val name: String,
    val type: String,
    val alias: String? = null,
    val description: String? = null,
    val recordPeriod: Int? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = TransactionReference(name, type, tenant)

    constructor(transaction: CfgTransaction) : this(
        tenant = transaction.tenant.getReference(),
        name = transaction.name,
        type = transaction.type.toShortName(),
        alias = transaction.alias,
        description = transaction.description,
        recordPeriod = transaction.recordPeriod,
        state = transaction.state?.toShortName(),
        userProperties = transaction.userProperties?.asCategorizedProperties(),
        folder = transaction.getFolderReference()
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgTransaction(service).also {
            applyDefaultValues()
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("name", name, it)
            setProperty("type", toCfgTransactionType(type), it)
            setFolder(folder, it)
        })

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgTransaction).also {
            setProperty(ALIAS, alias, it)
            setProperty("description", description, it)
            setProperty("recordPeriod", recordPeriod, it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            setProperty("state", ConfigurationObjects.toCfgObjectState(state), it)
        }

    override fun cloneBare() = null

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> =
        if (alias == null) setOf(ALIAS) else emptySet()

    override fun checkUnchangeableProperties(cfgObject: CfgObject) = checkUnchangeableProperties(this, cfgObject)

    override fun applyDefaultValues() {
        // alias = name
        // description = ""
        // recordPeriod = 0
        // type = CfgTransactionType.CFGTRTNoTransactionType.toShortName()
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(tenant)
            .add(folder)
            .toSet()
}
