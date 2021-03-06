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
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPIVRProfile
import com.nuecho.mutagen.cli.asBoolean
import com.nuecho.mutagen.cli.getFolderReference
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgIVRProfileType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.mutagen.cli.models.configuration.reference.DNReference
import com.nuecho.mutagen.cli.models.configuration.reference.FolderReference
import com.nuecho.mutagen.cli.models.configuration.reference.GVPIVRProfileReference
import com.nuecho.mutagen.cli.models.configuration.reference.TenantReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.toShortName
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.GregorianCalendar

/**
 * Customer and reseller properties are not defined as they are not in use in GA.
 * @See https://docs.genesys.com/Documentation/PSDK/8.5.x/ConfigLayerRef/CfgGVPIVRProfile
 */
data class GVPIVRProfile(
    val name: String,
    val tenant: TenantReference? = null,
    val displayName: String? = null,
    val type: String? = null,
    val notes: String? = null,
    val description: String? = null,
    val startServiceDate: Date? = null,
    val endServiceDate: Date? = null,
    @get:JsonProperty("isProvisioned")
    val isProvisioned: Boolean? = null,
    val tfn: List<String>? = null,
    val status: String? = null,
    val dids: List<DNReference>? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject {

    @get:JsonIgnore
    override val reference = GVPIVRProfileReference(name)

    constructor(gvpivrProfile: CfgGVPIVRProfile) : this(
        name = gvpivrProfile.name,
        tenant = gvpivrProfile.tenant.getReference(),
        displayName = gvpivrProfile.displayName,
        type = gvpivrProfile.type?.toShortName(),
        notes = gvpivrProfile.notes,
        description = gvpivrProfile.description,
        startServiceDate = gvpivrProfile.startServiceDate?.time,
        endServiceDate = gvpivrProfile.endServiceDate?.time,
        isProvisioned = gvpivrProfile.isProvisioned?.asBoolean(),
        tfn = gvpivrProfile.tfn?.split(',')?.map { it.trim() }?.toList(),
        status = gvpivrProfile.status,
        dids = gvpivrProfile.diDs?.map { it.getReference() },
        state = gvpivrProfile.state?.toShortName(),
        userProperties = gvpivrProfile.userProperties?.asCategorizedProperties(),
        folder = gvpivrProfile.getFolderReference()
    )

    override fun createCfgObject(service: ConfService) =
        updateCfgObject(service, CfgGVPIVRProfile(service)).also {
            setProperty("name", name, it)
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setFolder(folder, it, service)
        }

    override fun updateCfgObject(service: ConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgGVPIVRProfile).also {

            setProperty(DISPLAY_NAME, displayName, it)
            setProperty("type", toCfgIVRProfileType(type), it)
            setProperty("notes", notes, it)
            setProperty("description", description, it)
            setDateProperty("startServiceDate", startServiceDate, it)
            setDateProperty("endServiceDate", endServiceDate, it)
            setProperty("isProvisioned", toCfgFlag(isProvisioned), it)
            setProperty("tfn", tfn?.joinToString(), it)
            setProperty("status", status, it)
            setProperty("DIDDBIDs", dids?.map { did -> service.getObjectDbid(did) }, it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            setProperty("state", toCfgObjectState(state), it)
        }

    override fun cloneBare() = GVPIVRProfile(
        name = name,
        displayName = displayName,
        tenant = tenant
    )

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> {
        val missingMandatoryProperties = mutableSetOf<String>()
        displayName ?: missingMandatoryProperties.add(DISPLAY_NAME)
        tenant ?: missingMandatoryProperties.add(TENANT)

        return missingMandatoryProperties
    }

    override fun checkUnchangeableProperties(cfgObject: CfgObject) =
        checkUnchangeableProperties(this, cfgObject).also { unchangeableProperties ->
            (cfgObject as CfgGVPIVRProfile).also {
                tenant?.run { if (it.tenant?.getReference() != tenant) unchangeableProperties.add(TENANT) }
            }
        }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(tenant)
            .add(dids)
            .add(folder)
            .toSet()
}

fun setDateProperty(field: String, serviceDate: Date?, cfgObject: CfgGVPIVRProfile) {
    // XXX technically, there's no reason why we should stick to GMT.
    // While resellers do have a timeZone, ivr profiles do not.
    val zoneId = ZoneId.of("GMT", ZoneId.SHORT_IDS)
    if (serviceDate != null) {
        val date = GregorianCalendar.from(ZonedDateTime.ofInstant(serviceDate.toInstant(), zoneId))
        setProperty(field, date, cfgObject)
    }
}
