package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgAppRank
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.core.InitializingBean
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgAppType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgRank
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

/**
 * Unused address and phones properties are not defined.
 * Ignored field passwordUpdatingDate.
 */
data class Person(
    val tenant: TenantReference,
    val employeeId: String,
    var userName: String? = null,
    val externalId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val password: String? = null,
    val passwordHashAlgorithm: Int? = null,
    val changePasswordOnNextLogin: Boolean? = null,
    val emailAddress: String? = null,
    val state: String? = null,
    val agent: Boolean? = null,
    val externalAuth: Boolean? = null,
    val appRanks: Map<String, String>? = null,
    val agentInfo: AgentInfo? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject, InitializingBean {
    @get:JsonIgnore
    override val reference = PersonReference(employeeId, tenant)

    constructor(person: CfgPerson) : this(
        tenant = person.tenant.getReference(),
        employeeId = person.employeeID,
        externalId = person.externalID,
        firstName = person.firstName,
        lastName = person.lastName,
        userName = person.userName,
        password = person.password,
        passwordHashAlgorithm = person.passwordHashAlgorithm,
        changePasswordOnNextLogin = person.changePasswordOnNextLogin?.asBoolean(),
        emailAddress = person.emailAddress,
        state = person.state?.toShortName(),
        agent = person.isAgent?.asBoolean(),
        externalAuth = person.isExternalAuth?.asBoolean(),
        appRanks = person.appRanks?.map { it.appType.toShortName() to it.appRank.toShortName() }?.toMap(),
        userProperties = person.userProperties?.asCategorizedProperties(),
        folder = person.getFolderReference(),
        agentInfo = if (person.agentInfo != null) AgentInfo(person.agentInfo) else null
    )

    override fun createCfgObject(service: IConfService) =
        updateCfgObject(service, CfgPerson(service)).also {
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("employeeID", employeeId, it)
            setFolder(folder, it)
        }

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgPerson).also {
            setProperty(USER_NAME, userName, it)
            setProperty("externalID", externalId, it)
            setProperty("firstName", firstName, it)
            setProperty("lastName", lastName, it)
            setProperty("password", password, it)
            setProperty("passwordHashAlgorithm", passwordHashAlgorithm, it)
            setProperty("changePasswordOnNextLogin", toCfgFlag(changePasswordOnNextLogin), it)
            setProperty("emailAddress", emailAddress, it)
            setProperty("state", toCfgObjectState(state), it)
            setProperty("isAgent", toCfgFlag(agent), it)
            setProperty("isExternalAuth", toCfgFlag(externalAuth), it)
            setProperty("appRanks", toCfgAppRankList(appRanks, it), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            setProperty(
                "agentInfo",
                agentInfo?.toUpdatedCfgAgentInfo( service, it.agentInfo ?: CfgAgentInfo(service, it)),
                it
            )
        }

    override fun cloneBare() = Person(
        tenant = tenant,
        employeeId = employeeId,
        userName = userName
    )

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> =
        if (userName == null) setOf(USER_NAME) else emptySet()

    override fun checkUnchangeableProperties(cfgObject: CfgObject) = checkUnchangeableProperties(this, cfgObject)

    override fun afterPropertiesSet() {
        agentInfo?.updateTenantReferences(tenant)
    }

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(tenant)
            .add(folder)
            .add(agentInfo?.getReferences())
            .toSet()
}

private fun toCfgAppRankList(appRankMap: Map<String, String>?, person: CfgPerson) =
    appRankMap?.map { (type, rank) ->
        val appRank = CfgAppRank(person.configurationService, person)
        appRank.appType = toCfgAppType(type)
        appRank.appRank = toCfgRank(rank)
        appRank
    }?.toList()
