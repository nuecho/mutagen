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
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPReseller
import com.nuecho.mutagen.cli.asBoolean
import com.nuecho.mutagen.cli.core.InitializingBean
import com.nuecho.mutagen.cli.getFolderReference
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.mutagen.cli.models.configuration.reference.FolderReference
import com.nuecho.mutagen.cli.models.configuration.reference.GVPResellerReference
import com.nuecho.mutagen.cli.models.configuration.reference.TenantReference
import com.nuecho.mutagen.cli.models.configuration.reference.TimeZoneReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.services.getObjectDbid
import com.nuecho.mutagen.cli.toShortName
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.GregorianCalendar

/**
 * @See https://docs.genesys.com/Documentation/PSDK/8.5.x/ConfigLayerRef/CfgGVPReseller
 */
data class GVPReseller(
    val tenant: TenantReference,
    val name: String,
    val timeZone: TimeZoneReference? = null,
    val startDate: Date? = null, // FIXME we lose milliseconds somewhere down the line might be jackson or the confserv
    val displayName: String? = null,
    @get:JsonProperty("isParentNSP")
    val isParentNSP: Boolean? = null,
    val notes: String? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {

    @get:JsonIgnore
    override val reference = GVPResellerReference(name, tenant)

    constructor(gvpReseller: CfgGVPReseller) : this(
        tenant = gvpReseller.tenant.getReference(),
        name = gvpReseller.name,
        displayName = gvpReseller.displayName,
        startDate = gvpReseller.startDate?.time,
        isParentNSP = gvpReseller.isParentNSP?.asBoolean(),
        timeZone = gvpReseller.timeZone?.getReference(),
        notes = gvpReseller.notes,
        state = gvpReseller.state?.toShortName(),
        userProperties = gvpReseller.userProperties?.asCategorizedProperties(),
        folder = gvpReseller.getFolderReference()
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgGVPReseller(service)).also {
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("name", name, it)
            setFolder(folder, it)
        }

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgGVPReseller).also {
            setProperty(DISPLAY_NAME, displayName ?: name, it)
            setProperty("notes", notes, it)
            setProperty("timeZoneDBID", service.getObjectDbid(timeZone ?: TimeZoneReference(tenant = tenant)), it)
            setProperty(IS_PARENT_NSP, toCfgFlag(isParentNSP ?: false), it)

            if (startDate != null) {
                val zoneId = ZoneId.of(timeZone?.primaryKey ?: "GMT", ZoneId.SHORT_IDS)
                val date = GregorianCalendar.from(
                    ZonedDateTime.ofInstant(startDate.toInstant(), zoneId)
                )
                setProperty("startDate", date, it)
            }
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            setProperty("state", toCfgObjectState(state), it)
        }

    override fun cloneBare() = GVPReseller(tenant, name)

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> = emptySet()

    override fun checkUnchangeableProperties(cfgObject: CfgObject) = checkUnchangeableProperties(this, cfgObject)

    override fun afterPropertiesSet() {
        timeZone?.tenant = tenant
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(tenant)
            .add(timeZone)
            .add(folder)
            .toSet()
}
