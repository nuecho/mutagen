package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgIVR
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgIVRType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.ApplicationReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.IVRReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

data class Ivr(
    val name: String,
    val description: String? = null,
    val ivrServer: ApplicationReference? = null, // the type of this application must be CfgIVRInterfaceServer
    val tenant: TenantReference? = null,
    val type: String? = null,
    val version: String? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = IVRReference(name)

    constructor(cfgIvr: CfgIVR) : this(
        name = cfgIvr.name,
        description = cfgIvr.description,
        ivrServer = cfgIvr.ivrServer?.getReference(),
        tenant = cfgIvr.tenant?.getReference(),
        type = cfgIvr.type?.toShortName(),
        version = cfgIvr.version,
        state = cfgIvr.state.toShortName(),
        userProperties = cfgIvr.userProperties?.asCategorizedProperties(),
        folder = cfgIvr.getFolderReference()
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgIVR(service))

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgIVR).also { cfgIvr ->
            ConfigurationObjects.setProperty("name", name, cfgIvr)
            ConfigurationObjects.setProperty("tenantDBID", service.getObjectDbid(tenant), cfgIvr)
            ConfigurationObjects.setProperty("description", description, cfgIvr)
            ConfigurationObjects.setProperty(TYPE, toCfgIVRType(type), cfgIvr)
            ConfigurationObjects.setProperty("version", version, cfgIvr)
            ConfigurationObjects.setProperty("IVRServerDBID", service.getObjectDbid(ivrServer), cfgIvr)
            ConfigurationObjects.setProperty("state", toCfgObjectState(state), cfgIvr)
            ConfigurationObjects.setProperty("userProperties", toKeyValueCollection(userProperties), cfgIvr)
            ConfigurationObjects.setFolder(folder, cfgIvr)
        }

    override fun cloneBare() = Ivr(name = name, tenant = tenant, type = type, version = version)

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> {
        val missingMandatoryProperties = mutableSetOf<String>()
        tenant ?: missingMandatoryProperties.add(TENANT)
        type ?: missingMandatoryProperties.add(TYPE)
        version ?: missingMandatoryProperties.add(VERSION)
        return missingMandatoryProperties
    }

    override fun getReferences() = referenceSetBuilder()
        .add(folder)
        .add(ivrServer)
        .add(tenant)
        .toSet()
}
