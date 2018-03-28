package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.nuecho.genesys.cli.asMap
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toKeyValueCollection
import com.nuecho.genesys.cli.toShortName

data class Skill(
    val name: String,
    val state: String? = null,
    @JsonSerialize(using = KeyValueCollectionSerializer::class)
    @JsonDeserialize(using = KeyValueCollectionDeserializer::class)
    val userProperties: Map<String, Any>? = null

) : ConfigurationObject {

    override val primaryKey: String
        @JsonIgnore
        get() = name

    constructor(skill: CfgSkill) : this(
        name = skill.name,
        state = skill.state?.toShortName(),
        userProperties = skill.userProperties?.asMap()
    )
}

fun CfgSkill.import(skill: Skill) {
    setProperty("name", skill.name, this)
    setProperty("userProperties", toKeyValueCollection(skill.userProperties), this)
    setProperty("state", ConfigurationObjects.toCfgObjectState(skill.state), this)
}
