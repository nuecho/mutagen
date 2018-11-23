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
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNAccessNumber
import com.nuecho.mutagen.cli.asBoolean
import com.nuecho.mutagen.cli.core.InitializingBean
import com.nuecho.mutagen.cli.getFolderReference
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgDNRegisterFlag
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgDNType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgRouteType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.mutagen.cli.models.configuration.reference.DNGroupReference
import com.nuecho.mutagen.cli.models.configuration.reference.DNReference
import com.nuecho.mutagen.cli.models.configuration.reference.FolderReference
import com.nuecho.mutagen.cli.models.configuration.reference.ObjectiveTableReference
import com.nuecho.mutagen.cli.models.configuration.reference.SwitchReference
import com.nuecho.mutagen.cli.models.configuration.reference.TenantReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.toShortName

/**
 * @See https://docs.genesys.com/Documentation/PSDK/8.5.x/ConfigLayerRef/CfgDN
 */
data class DN(
    val tenant: TenantReference,
    val number: String,
    val switch: SwitchReference,
    val type: String,

    val registerAll: String? = null,
    val switchSpecificType: Int? = null,

    val association: String? = null,

    val routeType: String? = null,
    val destinationDNs: List<DNReference>? = null,

    val dnLoginID: String? = null,
    val group: DNGroupReference? = null,
    val trunks: Int? = null,
    val override: String? = null,

    val state: String? = null,

    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null,

    val name: String? = null,

    val useOverride: Boolean? = null,
    val accessNumbers: List<DNAccessNumber>? = null,

    val site: FolderReference? = null,
    val contract: ObjectiveTableReference? = null

) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = DNReference(number, switch, type, name, tenant)

    constructor(dn: CfgDN) : this(
        tenant = dn.tenant.getReference(),
        number = dn.number,
        switch = dn.switch.getReference(),
        type = dn.type!!.toShortName(),

        registerAll = dn.registerAll?.toShortName(),
        switchSpecificType = dn.switchSpecificType,

        association = dn.association,

        routeType = dn.routeType.toShortName(),
        destinationDNs = dn.destDNs?.map { it.getReference() },

        dnLoginID = dn.dnLoginID,
        group = dn.group?.getReference(),
        trunks = dn.trunks,
        override = dn.override,

        state = dn.state?.toShortName(),
        userProperties = dn.userProperties?.asCategorizedProperties(),
        folder = dn.getFolderReference(),

        name = dn.name,

        useOverride = dn.useOverride?.asBoolean(),

        accessNumbers = dn.accessNumbers?.map { DNAccessNumber(it) },
        site = dn.site?.getReference(),
        contract = dn.contract?.getReference()
    )

    override fun createCfgObject(service: ConfService) =
        updateCfgObject(service, CfgDN(service)).also {
            setProperty("number", number, it)
            setProperty("switchDBID", service.getObjectDbid(switch), it)
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("type", toCfgDNType(type), it)
            setFolder(folder, it, service)
        }

    override fun updateCfgObject(service: ConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgDN).also {
            // Switch
            setProperty("registerAll", toCfgDNRegisterFlag(registerAll), it)
            setProperty("switchSpecificType", switchSpecificType, it)

            setProperty("association", association, it)

            // Routing
            setProperty(ROUTE_TYPE, toCfgRouteType(routeType), it)
            setProperty("destDNDBIDs", destinationDNs?.mapNotNull { service.getObjectDbid(it) }, it)

            setProperty("DNLoginID", dnLoginID, it)

            setProperty("groupDBID", service.getObjectDbid(group), it)
            setProperty("trunks", trunks, it)
            setProperty("override", override, it)

            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)

            setProperty("name", name, it)

            setProperty("useOverride", toCfgFlag(useOverride), it)
            setProperty("accessNumbers", toCfgDNAccessNumberList(accessNumbers, it, service), it)

            setProperty("siteDBID", service.getObjectDbid(site), it)

            setProperty("contractDBID", service.getObjectDbid(contract), it)
        }

    override fun cloneBare() = DN(
        tenant = tenant,
        number = number,
        switch = switch,
        type = type,
        routeType = routeType
    )

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> =
        if (routeType == null) setOf(ROUTE_TYPE) else emptySet()

    override fun applyDefaultValues() {
        // registerAll = CfgDNRegisterFlag.CFGDRTrue.toShortName()
        // switchSpecificType  = 1
        // routeType = CfgRouteType.CFGDirect.toShortName()
        // destinationDNs = emptyList()
        // trunks  = 0
        // useOverride = CfgFlag.CFGTrue.asBoolean(),
        // accessNumbers = emptyList(),
    }

    override fun checkUnchangeableProperties(cfgObject: CfgObject) = checkUnchangeableProperties(this, cfgObject)

    override fun afterPropertiesSet() {
        switch.tenant = tenant
        destinationDNs?.forEach { it.updateTenantReferences(tenant) }
        group?.tenant = tenant
        accessNumbers?.forEach { it.updateTenantReferences(tenant) }
        contract?.tenant = tenant
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(tenant)
            .add(switch)
            .add(destinationDNs)
            .add(group)
            .add(accessNumbers?.map { it.switch })
            .add(site)
            .add(contract)
            .add(folder)
            .toSet()
}

fun toCfgDNAccessNumberList(accessNumbers: List<DNAccessNumber>?, dn: CfgDN, service: ConfService) =
    if (accessNumbers == null) null
    else {
        accessNumbers.map {
            CfgDNAccessNumber(service, dn).apply {
                number = it.number
                switchDBID = service.getObjectDbid(it.switch)
            }
        }
    }
