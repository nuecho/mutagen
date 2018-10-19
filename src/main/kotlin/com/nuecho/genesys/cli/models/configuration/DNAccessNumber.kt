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

import com.genesyslab.platform.applicationblocks.com.objects.CfgDNAccessNumber
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference

data class DNAccessNumber(
    val number: String,
    val switch: SwitchReference
) {
    constructor(dnAccessNumber: CfgDNAccessNumber) : this(
        number = dnAccessNumber.number,
        switch = dnAccessNumber.switch.getReference()
    )

    @Suppress("DataClassContainsFunctions")
    fun updateTenantReferences(tenant: TenantReference) {
        switch.tenant = tenant
    }
}
