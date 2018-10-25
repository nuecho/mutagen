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

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitchAccessCode
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgRouteType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjects.toCfgTargetType
import com.nuecho.mutagen.cli.models.configuration.reference.SwitchReference
import com.nuecho.mutagen.cli.models.configuration.reference.TenantReference
import com.nuecho.mutagen.cli.services.getObjectDbid
import com.nuecho.mutagen.cli.toShortName

/**
 * Note the possible values of field CfgSwitchAccessCode.switchDBID.
 * - if > 0, switchDBID points to the Switch to which this access code is assigned
 * - if = 0, there is no switch to which this access code is assigned - this access code is a "default access code"
 * Currently, having the `switch` field set to null mean that switchBBID will be set to 0.
 * TODO: This is something we'll need to address when adding the Update functionnality
 */
data class SwitchAccessCode(
    val switch: SwitchReference? = null,
    val accessCode: String? = null,
    val targetType: String,
    val routeType: String,
    val dnSource: String? = null,
    val destinationSource: String? = null,
    val locationSource: String? = null,
    val dnisSource: String? = null,
    val reasonSource: String? = null,
    val extensionSource: String? = null
) {
    constructor(switchAccessCode: CfgSwitchAccessCode) : this(
        switch = if (switchAccessCode.switchDBID == 0) null else switchAccessCode.switch.getReference(),
        accessCode = switchAccessCode.accessCode,
        targetType = switchAccessCode.targetType.toShortName(),
        routeType = switchAccessCode.routeType.toShortName(),
        dnSource = switchAccessCode.dnSource,
        destinationSource = switchAccessCode.destinationSource,
        locationSource = switchAccessCode.locationSource,
        dnisSource = switchAccessCode.dnisSource,
        reasonSource = switchAccessCode.reasonSource,
        extensionSource = switchAccessCode.extensionSource
    )

    fun toCfgSwitchAccessCode(service: IConfService, parent: CfgSwitch) = CfgSwitchAccessCode(service, parent).also {
        val switchDbid = if (switch == null) 0 else service.getObjectDbid(switch)

        setProperty("switchDBID", switchDbid, it)
        setProperty("accessCode", accessCode, it)
        setProperty("targetType", toCfgTargetType(targetType), it)
        setProperty("routeType", toCfgRouteType(routeType), it)
        setProperty("dnSource", dnSource, it)
        setProperty("destinationSource", destinationSource, it)
        setProperty("locationSource", locationSource, it)
        setProperty("dnisSource", dnisSource, it)
        setProperty("reasonSource", reasonSource, it)
        setProperty("extensionSource", extensionSource, it)
    }

    @Suppress("DataClassContainsFunctions")
    fun updateTenantReferences(tenant: TenantReference) {
        switch?.tenant = tenant
    }
}
