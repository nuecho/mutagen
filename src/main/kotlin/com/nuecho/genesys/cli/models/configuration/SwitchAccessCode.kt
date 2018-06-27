package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitchAccessCode
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgRouteType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgTargetType
import com.nuecho.genesys.cli.models.configuration.reference.SwitchReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.toShortName

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
