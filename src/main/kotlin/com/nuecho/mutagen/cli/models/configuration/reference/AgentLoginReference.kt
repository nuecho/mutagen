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

package com.nuecho.mutagen.cli.models.configuration.reference

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.queries.CfgAgentLoginQuery
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectNotFoundException
import com.nuecho.mutagen.cli.services.getObjectDbid

data class AgentLoginReference(
    val loginCode: String,
    val switch: SwitchReference
) : ConfigurationObjectReference<CfgAgentLogin>(CfgAgentLogin::class.java) {

    override fun toQuery(service: IConfService) = CfgAgentLoginQuery().also {
        it.loginCode = loginCode
        it.switchDbid = service.getObjectDbid(switch) ?: throw ConfigurationObjectNotFoundException(switch)
    }

    override fun compareTo(other: ConfigurationObjectReference<*>): Int {
        if (other !is AgentLoginReference) return super.compareTo(other)

        return Comparator
            .comparing(AgentLoginReference::switch)
            .thenComparing(AgentLoginReference::loginCode)
            .compare(this, other)
    }

    override fun toString() = "loginCode: '$loginCode', switch: '$switch'"

    @Suppress("DataClassContainsFunctions")
    fun updateTenantReferences(tenantReference: TenantReference) {
        switch.tenant = tenantReference
    }
}
