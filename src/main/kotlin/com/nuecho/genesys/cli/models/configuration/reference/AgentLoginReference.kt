package com.nuecho.genesys.cli.models.configuration.reference

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.queries.CfgAgentLoginQuery
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectNotFoundException
import com.nuecho.genesys.cli.services.getObjectDbid

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
