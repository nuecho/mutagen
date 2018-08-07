package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNInfo
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType.CFGDN
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgDNGroupType
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.DNGroupReference
import com.nuecho.genesys.cli.models.configuration.reference.DNReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName

data class DNGroup(
    val group: Group,
    val dns: List<DNInfo>? = null,
    val type: String? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {

    @get:JsonIgnore
    override val reference = DNGroupReference(group.name, group.tenant)

    override val userProperties: CategorizedProperties?
        @JsonIgnore
        get() = group.userProperties

    constructor(dnGroup: CfgDNGroup) : this(
        group = Group(dnGroup.groupInfo),
        dns = dnGroup.dNs?.map { cfgDNInfo ->
            val dn = dnGroup.configurationService.retrieveObject(CFGDN, cfgDNInfo.dndbid) as CfgDN
            DNInfo(dn.getReference(), cfgDNInfo.trunks)
        },
        type = dnGroup.type.toShortName(),
        folder = dnGroup.getFolderReference()
    )

    constructor(tenant: TenantReference, name: String) : this(
        group = Group(tenant, name)
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgDNGroup(service)).also {
            setProperty(TYPE, toCfgDNGroupType(type), it)
        }

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgDNGroup).also { cfgDNGroup ->
            val groupInfo = group.toCfgGroup(service, cfgDNGroup).also {
                cfgDNGroup.dbid?.let { dbid ->
                    it.dbid = dbid
                }
            }

            setProperty("groupInfo", groupInfo, cfgDNGroup)
            setProperty(
                "DNs",
                dns?.map { dnInfo ->
                    val dn = service.retrieveObject(dnInfo.dn)!!
                    CfgDNInfo(service, cfgDNGroup).also {
                        setProperty("DNDBID", dn.dbid, it)
                        setProperty("trunks", dnInfo.trunks, it)
                    }
                },
                cfgDNGroup
            )
            setFolder(folder, cfgDNGroup)
        }

    override fun cloneBare() = DNGroup(
        group = Group(group.tenant, group.name),
        type = type
    )

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> =
        if (type == null) setOf(TYPE) else emptySet()

    override fun checkUnchangeableProperties(cfgObject: CfgObject): Set<String> {
        (cfgObject as CfgDNGroup).also {
            type?.run { if (this.toLowerCase() != it.type?.toShortName()) return setOf(TYPE) }
        }

        return emptySet()
    }

    override fun afterPropertiesSet() {
        group.updateTenantReferences()
        dns?.forEach {
            it.dn.tenant = group.tenant
            it.dn.switch.tenant = group.tenant
        }
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(dns?.map { it.dn })
            .add(group.getReferences())
            .add(folder)
            .toSet()
}

data class DNInfo(
    val dn: DNReference,
    val trunks: Int? = null
)
