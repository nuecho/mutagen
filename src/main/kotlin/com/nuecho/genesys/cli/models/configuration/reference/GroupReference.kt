package com.nuecho.genesys.cli.models.configuration.reference

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "groupType")
@JsonSubTypes(
    Type(value = AgentGroupReference::class, name = "agentgroup"),
    Type(value = PlaceGroupReference::class, name = "placegroup")
)
interface GroupReference {
    var tenant: TenantReference?
}
