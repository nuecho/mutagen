package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgHost
import com.genesyslab.platform.applicationblocks.com.objects.CfgOS
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.toShortName

data class OS(
    val type: String,
    val version: String? = null
) {
    constructor(os: CfgOS) : this(
        type = os.oStype.toShortName(),
        version = os.oSversion
    )

    fun toCfgOs(service: IConfService, parent: CfgHost) = CfgOS(service, parent).also { cfgOs ->
        setProperty("OStype", ConfigurationObjects.toCfgOsType(type), cfgOs)
        setProperty("OSversion", version, cfgOs)
    }
}
