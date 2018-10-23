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

package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore

data class Configuration(
    val __metadata__: Metadata,
    val accessGroups: List<AccessGroup> = emptyList(),
    val actionCodes: List<ActionCode> = emptyList(),
    val agentGroups: List<AgentGroup> = emptyList(),
    val alarmConditions: List<AlarmCondition> = emptyList(),
    val applications: List<Application> = emptyList(),
    val appPrototypes: List<AppPrototype> = emptyList(),
    val campaignGroups: List<CampaignGroup> = emptyList(),
    val campaigns: List<Campaign> = emptyList(),
    val dnGroups: List<DNGroup> = emptyList(),
    val dns: List<DN> = emptyList(),
    val enumerators: List<Enumerator> = emptyList(),
    val enumeratorValues: List<EnumeratorValue> = emptyList(),
    val fields: List<Field> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val gvpCustomers: List<GVPCustomer> = emptyList(),
    val gvpIVRProfiles: List<GVPIVRProfile> = emptyList(),
    val gvpResellers: List<GVPReseller> = emptyList(),
    val hosts: List<Host> = emptyList(),
    val ivrs: List<Ivr> = emptyList(),
    val persons: List<Person> = emptyList(),
    val physicalSwitches: List<PhysicalSwitch> = emptyList(),
    val placeGroups: List<PlaceGroup> = emptyList(),
    val places: List<Place> = emptyList(),
    val roles: List<Role> = emptyList(),
    val scripts: List<Script> = emptyList(),
    val services: List<Service> = emptyList(),
    val skills: List<Skill> = emptyList(),
    val switches: List<Switch> = emptyList(),
    val tenants: List<Tenant> = emptyList(),
    val transactions: List<Transaction> = emptyList()
) {

    @get:JsonIgnore
    val asList by lazy {
        listOf(
            accessGroups,
            actionCodes,
            agentGroups,
            alarmConditions,
            applications,
            appPrototypes,
            campaignGroups,
            campaigns,
            dnGroups,
            dns,
            enumerators,
            enumeratorValues,
            fields,
            folders,
            gvpCustomers,
            gvpIVRProfiles,
            gvpResellers,
            hosts,
            ivrs,
            persons,
            physicalSwitches,
            placeGroups,
            places,
            roles,
            scripts,
            services,
            skills,
            switches,
            tenants,
            transactions
        ).flatMap { it }
    }

    @get:JsonIgnore
    val asMapByReference by lazy {
        asList.map { it.reference to it }.toMap()
    }

    companion object {
        fun interpolateVariables(
            configurationString: String,
            variables: Map<String, String>
        ): String {
            val regex = Regex("\\$\\{([a-zA-Z_][a-zA-Z0-9_]*)}")
            if (!regex.containsMatchIn(configurationString)) return configurationString

            return regex.replace(configurationString) { match ->
                val variable = match.groupValues[1]
                variables[variable]
                        ?: throw UndefinedVariableException(
                            "Interpolation of variable [$variable] failed. Variable is undefined."
                        )
            }
        }
    }
}

class UndefinedVariableException(override val message: String?) : Exception(message)
