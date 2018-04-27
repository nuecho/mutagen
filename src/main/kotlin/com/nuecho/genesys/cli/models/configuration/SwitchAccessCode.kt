package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitchAccessCode
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgRouteType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgTargetType
import com.nuecho.genesys.cli.services.getSwitchDbid
import com.nuecho.genesys.cli.toShortName

/**
 * Note the possible values of field CfgSwitchAccessCode.switchDBID.
 * - if > 0, switchDBID points to the Switch to which this access code is assigned
 * - if = 0, there is no switch to which this access code is assigned - this access code is a "default access code"
 * In our model, we represent `switchDBID = 0` by `switch = ""`
 */
data class SwitchAccessCode(
    val switch: String? = null,
    val accessCode: String? = null,
    val targetType: String? = null,
    val routeType: String? = null,
    val dnSource: String? = null,
    val destinationSource: String? = null,
    val locationSource: String? = null,
    val dnisSource: String? = null,
    val reasonSource: String? = null,
    val extensionSource: String? = null
) {
    constructor(switchAccessCode: CfgSwitchAccessCode) : this(
        switch = if (switchAccessCode.switchDBID == 0) "" else switchAccessCode.switch.name,
        accessCode = switchAccessCode.accessCode,
        targetType = switchAccessCode.targetType?.toShortName(),
        routeType = switchAccessCode.routeType?.toShortName(),
        dnSource = switchAccessCode.dnSource,
        destinationSource = switchAccessCode.destinationSource,
        locationSource = switchAccessCode.locationSource,
        dnisSource = switchAccessCode.dnisSource,
        reasonSource = switchAccessCode.reasonSource,
        extensionSource = switchAccessCode.extensionSource
    )

    fun toCfgSwitchAccessCode(service: IConfService, parent: CfgSwitch) = CfgSwitchAccessCode(service, parent).also {
        val switchDbid = when (switch) {
            null -> null
            "" -> 0
            else -> service.getSwitchDbid(switch)
        }

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
}
