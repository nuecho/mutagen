package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.objects.CfgAppRank
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.asMap
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgAppType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgRank
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.toShortName

private const val DEFAULT_TENANT_DBID = 101

/**
 * Unused address and phones properties are not defined.
 */
data class Person(
    val employeeId: String,
    val userName: String,
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
    @JsonSerialize(using = KeyValueCollectionSerializer::class)
    @JsonDeserialize(using = KeyValueCollectionDeserializer::class)
    val userProperties: Map<String, Any>? = null,
    val agentInfo: AgentInfo? = null
) : ConfigurationObject {

    override val primaryKey: String
        @JsonIgnore
        get() = employeeId

    constructor(person: CfgPerson) : this(
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
        userProperties = person.userProperties?.asMap(),
        agentInfo = if (person.agentInfo != null) AgentInfo(person.agentInfo) else null
    )
}

fun CfgPerson.import(person: Person) {
    setProperty("tenantDBID", DEFAULT_TENANT_DBID) // TODO: Proper tenant management
    setProperty("employeeID", person.employeeId, this)
    setProperty("userName", person.userName, this)
    setProperty("externalID", person.externalId, this)
    setProperty("firstName", person.firstName, this)
    setProperty("lastName", person.lastName, this)
    setProperty("password", person.password, this)
    setProperty("passwordHashAlgorithm", person.passwordHashAlgorithm, this)
    setProperty("PasswordUpdatingDate", person.passwordUpdatingDate, this)
    setProperty("changePasswordOnNextLogin", toCfgFlag(person.changePasswordOnNextLogin), this)
    setProperty("emailAddress", person.emailAddress, this)
    setProperty("state", toCfgObjectState(person.state), this)
    setProperty("isAgent", toCfgFlag(person.agent), this)
    setProperty("isExternalAuth", toCfgFlag(person.externalAuth), this)
    setProperty("appRanks", toCfgAppRankList(person.appRanks, this), this)
    setProperty("userProperties", toKeyValueCollection(person.userProperties), this)
    setProperty("agentInfo", person.agentInfo?.toCfgAgentInfo(this), this)
}

private fun toCfgAppRankList(appRankMap: Map<String, String>?, person: CfgPerson) =
    appRankMap?.map { (type, rank) ->
        val appRank = CfgAppRank(person.configurationService, person)
        appRank.appType = toCfgAppType(type)
        appRank.appRank = toCfgRank(rank)
        appRank
    }?.toList()
