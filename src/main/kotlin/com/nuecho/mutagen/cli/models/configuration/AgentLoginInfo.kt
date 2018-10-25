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

package com.nuecho.mutagen.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLoginInfo
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.reference.AgentLoginReference
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.mutagen.cli.models.configuration.reference.TenantReference

@Suppress("DataClassContainsFunctions")
data class AgentLoginInfo(
    val agentLogin: AgentLoginReference,
    val wrapupTime: Int
) {
    constructor(agentLoginInfo: CfgAgentLoginInfo) : this(
        agentLogin = agentLoginInfo.agentLogin.getReference(),
        wrapupTime = agentLoginInfo.wrapupTime
    )

    fun updateTenantReferences(tenant: TenantReference) = agentLogin.updateTenantReferences(tenant)

    @JsonIgnore
    fun getReferences(): Set<ConfigurationObjectReference<*>> = setOf(agentLogin)
}
