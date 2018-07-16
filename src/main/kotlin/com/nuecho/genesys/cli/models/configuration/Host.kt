package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgHost
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGApplication
import com.nuecho.genesys.cli.Logging
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgHostType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.ApplicationReference
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.HostReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

/**
 * Not in use HWID, address, contactPersonDBID and comment properties are not defined.
 */
data class Host(
    val name: String,
    val ipAddress: String? = null,
    val lcaPort: String? = null,
    val osInfo: OS? = null,
    val scs: ApplicationReference? = null,
    val type: String? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = HostReference(name)

    constructor(host: CfgHost) : this(
        name = host.name,
        ipAddress = host.iPaddress,
        lcaPort = host.lcaPort,
        osInfo = OS(host.oSinfo),
        scs = host.configurationService.retrieveObject(CFGApplication, host.scsdbid)?.let {
            it as CfgApplication
            it.getReference()
        },
        type = host.type.toShortName(),
        state = host.state.toShortName(),
        userProperties = host.userProperties?.asCategorizedProperties(),
        folder = host.getFolderReference()
    ) {
        host.resources?.let { Logging.warn { "Unsupported ResourceObject collection in host object. Ignoring." } }
    }

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgHost(service))

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgHost).also { cfgHost ->
            setProperty("name", name, cfgHost)
            setProperty("IPaddress", ipAddress, cfgHost)
            setProperty("LCAPort", lcaPort, cfgHost)
            setProperty(
                "OSinfo",
                osInfo?.toCfgOs(service, cfgHost),
                cfgHost
            )
            setProperty(
                "SCSDBID",
                service.getObjectDbid(scs),
                cfgHost
            )
            setProperty(TYPE, toCfgHostType(type), cfgHost)

            setProperty("userProperties", toKeyValueCollection(userProperties), cfgHost)
            setProperty("state", toCfgObjectState(state), cfgHost)
            setFolder(folder, cfgHost)
        }

    override fun checkMandatoryProperties(): Set<String> {
        val missingMandatoryProperties = mutableSetOf<String>()

        if (lcaPort == null)
            missingMandatoryProperties.add("lcaPort")
        if (osInfo == null)
            missingMandatoryProperties.add("osInfo")
        if (type == null)
            missingMandatoryProperties.add(TYPE)

        return missingMandatoryProperties
    }

    override fun afterPropertiesSet() {}

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(folder)
            .add(scs)
            .toSet()
}
