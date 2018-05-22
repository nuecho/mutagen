package com.nuecho.genesys.cli.models.configuration.reference

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAccessGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgActionCode
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlaceGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgRole
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.applicationblocks.com.objects.CfgStatTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.applicationblocks.com.objects.CfgTransaction
import com.genesyslab.platform.applicationblocks.com.queries.CfgAccessGroupQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgActionCodeQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgAgentGroupQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgAgentLoginQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgApplicationQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgDNGroupQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgEnumeratorQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgFolderQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgObjectiveTableQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPersonQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPhysicalSwitchQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPlaceGroupQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgPlaceQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgRoleQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgScriptQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgSkillQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgStatTableQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgSwitchQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgTenantQuery
import com.genesyslab.platform.applicationblocks.com.queries.CfgTransactionQuery

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class AccessGroupReference(override val primaryKey: String) :
    SimpleObjectReference<CfgAccessGroup>(CfgAccessGroup::class.java) {
    override fun toQuery(service: IConfService) = CfgAccessGroupQuery(primaryKey)
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class ActionCodeReference(override val primaryKey: String) :
    SimpleObjectReference<CfgActionCode>(CfgActionCode::class.java) {
    override fun toQuery(service: IConfService) = CfgActionCodeQuery(primaryKey)
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class AgentGroupReference(override val primaryKey: String) :
    SimpleObjectReference<CfgAgentGroup>(CfgAgentGroup::class.java) {
    override fun toQuery(service: IConfService) = CfgAgentGroupQuery(primaryKey)
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class AgentLoginReference(override val primaryKey: String) :
    SimpleObjectReference<CfgAgentLogin>(CfgAgentLogin::class.java) {
    override fun toQuery(service: IConfService) = CfgAgentLoginQuery().apply { loginCode = primaryKey }
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class ApplicationReference(override val primaryKey: String) :
    SimpleObjectReference<CfgApplication>(CfgApplication::class.java) {
    override fun toQuery(service: IConfService) = CfgApplicationQuery(primaryKey)
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class DNGroupReference(override val primaryKey: String) :
    SimpleObjectReference<CfgDNGroup>(CfgDNGroup::class.java) {
    override fun toQuery(service: IConfService) = CfgDNGroupQuery(primaryKey)
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class EnumeratorReference(override val primaryKey: String) :
    SimpleObjectReference<CfgEnumerator>(CfgEnumerator::class.java) {
    override fun toQuery(service: IConfService) = CfgEnumeratorQuery(primaryKey)
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class FolderReference(override val primaryKey: String) :
    SimpleObjectReference<CfgFolder>(CfgFolder::class.java) {
    override fun toQuery(service: IConfService) = CfgFolderQuery(primaryKey)
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class ObjectiveTableReference(override val primaryKey: String) :
    SimpleObjectReference<CfgObjectiveTable>(CfgObjectiveTable::class.java) {
    override fun toQuery(service: IConfService) = CfgObjectiveTableQuery(primaryKey)
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class PersonReference(override val primaryKey: String) :
    SimpleObjectReference<CfgPerson>(CfgPerson::class.java) {
    override fun toQuery(service: IConfService) = CfgPersonQuery().apply { employeeId = primaryKey }
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class PhysicalSwitchReference(override val primaryKey: String) :
    SimpleObjectReference<CfgPhysicalSwitch>(CfgPhysicalSwitch::class.java) {
    override fun toQuery(service: IConfService) = CfgPhysicalSwitchQuery(primaryKey)
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class PlaceGroupReference(override val primaryKey: String) :
    SimpleObjectReference<CfgPlaceGroup>(CfgPlaceGroup::class.java) {
    override fun toQuery(service: IConfService) = CfgPlaceGroupQuery(primaryKey)
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class PlaceReference(override val primaryKey: String) :
    SimpleObjectReference<CfgPlace>(CfgPlace::class.java) {
    override fun toQuery(service: IConfService) = CfgPlaceQuery(primaryKey)
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class RoleReference(override val primaryKey: String) :
    SimpleObjectReference<CfgRole>(CfgRole::class.java) {
    override fun toQuery(service: IConfService) = CfgRoleQuery(primaryKey)
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class ScriptReference(override val primaryKey: String) :
    SimpleObjectReference<CfgScript>(CfgScript::class.java) {
    override fun toQuery(service: IConfService) = CfgScriptQuery(primaryKey)
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class SkillReference(override val primaryKey: String) :
    SimpleObjectReference<CfgSkill>(CfgSkill::class.java) {
    override fun toQuery(service: IConfService) = CfgSkillQuery(primaryKey)
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class StatTableReference(override val primaryKey: String) :
    SimpleObjectReference<CfgStatTable>(CfgStatTable::class.java) {
    override fun toQuery(service: IConfService) = CfgStatTableQuery(primaryKey)
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class SwitchReference(override val primaryKey: String) :
    SimpleObjectReference<CfgSwitch>(CfgSwitch::class.java) {
    override fun toQuery(service: IConfService) = CfgSwitchQuery(primaryKey)
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class TenantReference(override val primaryKey: String) :
    SimpleObjectReference<CfgTenant>(CfgTenant::class.java) {
    override fun toQuery(service: IConfService) = CfgTenantQuery(primaryKey).apply { allTenants = 1 }
}

@JsonSerialize(using = SimpleObjectReferenceSerializer::class)
@JsonDeserialize(using = SimpleObjectReferenceDeserializer::class)
class TransactionReference(override val primaryKey: String) :
    SimpleObjectReference<CfgTransaction>(CfgTransaction::class.java) {
    override fun toQuery(service: IConfService) = CfgTransactionQuery(primaryKey)
}
