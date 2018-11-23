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
import com.genesyslab.platform.applicationblocks.com.objects.CfgService
import com.genesyslab.platform.applicationblocks.com.objects.CfgSolutionComponent
import com.genesyslab.platform.applicationblocks.com.objects.CfgSolutionComponentDefinition
import com.nuecho.mutagen.cli.asBoolean
import com.nuecho.mutagen.cli.getFolderReference
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgAppType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgSolutionType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgStartupType
import com.nuecho.mutagen.cli.models.configuration.reference.ApplicationReference
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.mutagen.cli.models.configuration.reference.FolderReference
import com.nuecho.mutagen.cli.models.configuration.reference.ServiceReference
import com.nuecho.mutagen.cli.models.configuration.reference.TenantReference
import com.nuecho.mutagen.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.mutagen.cli.services.ConfService
import com.nuecho.mutagen.cli.toShortName

data class Service(
    val name: String,
    val assignedTenant: TenantReference? = null,
    val componentDefinitions: List<SolutionComponentDefinition>? = null,
    val components: List<SolutionComponent>? = null,
    val scs: ApplicationReference? = null,
    val solutionType: String? = null,
    val startupType: String? = null,
    val version: String? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject {

    @get:JsonIgnore
    override val reference = ServiceReference(name)

    constructor(cfgService: CfgService) : this(
        name = cfgService.name,
        assignedTenant = cfgService.assignedTenant?.run { TenantReference(name) },
        componentDefinitions = cfgService.componentDefinitions.map { SolutionComponentDefinition(it) },
        components = cfgService.components.map { SolutionComponent(it) },
        scs = cfgService.scs?.run { ApplicationReference(name) },
        solutionType = cfgService.solutionType.toShortName(),
        startupType = cfgService.startupType.toShortName(),
        version = cfgService.version,
        state = cfgService.state?.toShortName(),
        userProperties = cfgService.userProperties?.asCategorizedProperties(),
        folder = cfgService.getFolderReference()
    )

    override fun createCfgObject(service: ConfService) =
        updateCfgObject(service, CfgService(service)).also {
            setProperty("name", name, it)
            setProperty("solutionType", toCfgSolutionType(solutionType), it)
            setFolder(folder, it, service)
        }

    override fun updateCfgObject(service: ConfService, cfgObject: ICfgObject): CfgService =
        (cfgObject as CfgService).also {
            setProperty("components", components?.map { component ->
                component.toCfgSolutionComponent(it, service)
            }, it)
            setProperty("SCSDBID", service.getObjectDbid(scs), it)
            setProperty("assignedTenantDBID", service.getObjectDbid(assignedTenant), it)
            setProperty("version", version, it)
            setProperty("componentDefinitions", componentDefinitions?.map { component ->
                component.toCfgSolutionComponentDefinition(it)
            }, it)
            setProperty("startupType", toCfgStartupType(startupType), it)
            setProperty("state", toCfgObjectState(state), it)
            setProperty("userProperties", ConfigurationObjects.toKeyValueCollection(userProperties), it)
        }

    override fun cloneBare() = Service(name = name, solutionType = solutionType, version = version)

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> {
        val missingMandatoryProperties = mutableSetOf<String>()
        solutionType ?: missingMandatoryProperties.add(SOLUTION_TYPE)
        version ?: missingMandatoryProperties.add(VERSION)

        return missingMandatoryProperties
    }

    override fun checkUnchangeableProperties(cfgObject: CfgObject) =
        ConfigurationObjects.checkUnchangeableProperties(this, cfgObject).also { unchangeableProperties ->
            (cfgObject as CfgService).also {
                solutionType?.run {
                    if (this != it.solutionType?.toShortName()) unchangeableProperties.add(SOLUTION_TYPE)
                }
            }
        }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(components?.map { it.app })
            .add(folder)
            .add(scs)
            .add(assignedTenant)
            .toSet()
}

data class SolutionComponent(
    val app: ApplicationReference,
    val startupPriority: Int,
    @get:JsonProperty("isOptional")
    val isOptional: Boolean? = null
) {
    constructor(cfgSolutionComponent: CfgSolutionComponent) : this(
        app = cfgSolutionComponent.app.getReference(),
        startupPriority = cfgSolutionComponent.startupPriority,
        isOptional = cfgSolutionComponent.isOptional?.asBoolean()
    )

    fun toCfgSolutionComponent(cfgService: CfgService, service: ConfService): CfgSolutionComponent {
        val solutionComponent = CfgSolutionComponent(cfgService.configurationService, cfgService)

        setProperty("startupPriority", startupPriority, solutionComponent)
        setProperty("isOptional", toCfgFlag(isOptional), solutionComponent)
        setProperty("appDBID", service.getObjectDbid(app), solutionComponent)

        return solutionComponent
    }
}

data class SolutionComponentDefinition(
    val startupPriority: Int,
    val type: String,
    @get:JsonProperty("isOptional")
    val isOptional: Boolean? = null,
    val version: String? = null
) {
    constructor(cfgSolutionComponentDefinition: CfgSolutionComponentDefinition) : this(
        startupPriority = cfgSolutionComponentDefinition.startupPriority,
        type = cfgSolutionComponentDefinition.type.toShortName(),
        isOptional = cfgSolutionComponentDefinition.isOptional?.asBoolean(),
        version = cfgSolutionComponentDefinition.version
    )

    fun toCfgSolutionComponentDefinition(cfgService: CfgService): CfgSolutionComponentDefinition {
        val solutionComponentDefinition = CfgSolutionComponentDefinition(cfgService.configurationService, cfgService)

        setProperty("startupPriority", startupPriority, solutionComponentDefinition)
        setProperty("isOptional", toCfgFlag(isOptional), solutionComponentDefinition)
        setProperty("type", toCfgAppType(type), solutionComponentDefinition)
        setProperty("version", version, solutionComponentDefinition)

        return solutionComponentDefinition
    }
}
