package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAppRank
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.CREATED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectUpdateStatus.UNCHANGED
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgAppType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgRank
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.models.configuration.reference.PersonReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName

/**
 * Unused address and phones properties are not defined.
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
    val passwordUpdatingDate: Int? = null,
    val changePasswordOnNextLogin: Boolean? = null,
    val emailAddress: String? = null,
    val state: String? = null,
    val agent: Boolean? = null,
    val externalAuth: Boolean? = null,
    val appRanks: Map<String, String>? = null,
    val agentInfo: AgentInfo? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null
) : ConfigurationObject {
    @get:JsonIgnore
    override val reference = PersonReference(employeeId)

    constructor(person: CfgPerson) : this(
        tenant = TenantReference(person.tenant.name),
        employeeId = person.employeeID,
        externalId = person.externalID,
        firstName = person.firstName,
        lastName = person.lastName,
        userName = person.userName,
        password = person.password,
        passwordHashAlgorithm = person.passwordHashAlgorithm,
        passwordUpdatingDate = person.passwordUpdatingDate,
        changePasswordOnNextLogin = person.changePasswordOnNextLogin?.asBoolean(),
        emailAddress = person.emailAddress,
        state = person.state?.toShortName(),
        agent = person.isAgent?.asBoolean(),
        externalAuth = person.isExternalAuth?.asBoolean(),
        appRanks = person.appRanks?.map { it.appType.toShortName() to it.appRank.toShortName() }?.toMap(),
        userProperties = person.userProperties?.asCategorizedProperties(),
        agentInfo = if (person.agentInfo != null) AgentInfo(person.agentInfo) else null
    )

    override fun updateCfgObject(service: IConfService): ConfigurationObjectUpdateResult {
        service.retrieveObject(reference)?.let {
            return ConfigurationObjectUpdateResult(UNCHANGED, it)
        }

        CfgPerson(service).let {
            setProperty("tenantDBID", service.getObjectDbid(tenant), it)
            setProperty("employeeID", employeeId, it)
            setProperty("userName", userName ?: employeeId, it)
            setProperty("externalID", externalId, it)
            setProperty("firstName", firstName, it)
            setProperty("lastName", lastName, it)
            setProperty("password", password, it)
            setProperty("passwordHashAlgorithm", passwordHashAlgorithm, it)
            setProperty("PasswordUpdatingDate", passwordUpdatingDate, it)
            setProperty("changePasswordOnNextLogin", toCfgFlag(changePasswordOnNextLogin), it)
            setProperty("emailAddress", emailAddress, it)
            setProperty("state", toCfgObjectState(state), it)
            setProperty("isAgent", toCfgFlag(agent), it)
            setProperty("isExternalAuth", toCfgFlag(externalAuth), it)
            setProperty("appRanks", toCfgAppRankList(appRanks, it), it)
            setProperty("userProperties", toKeyValueCollection(userProperties), it)
            setProperty("agentInfo", agentInfo?.toCfgAgentInfo(it), it)
            return ConfigurationObjectUpdateResult(CREATED, it)
        }
    }
}

private fun toCfgAppRankList(appRankMap: Map<String, String>?, person: CfgPerson) =
    appRankMap?.map { (type, rank) ->
        val appRank = CfgAppRank(person.configurationService, person)
        appRank.appType = toCfgAppType(type)
        appRank.appRank = toCfgRank(rank)
        appRank
    }?.toList()
