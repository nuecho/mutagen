package com.nuecho.genesys.cli.models.configuration

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

    fun toUpdatedCfgOS(cfgOs: CfgOS) = cfgOs.also {
        setProperty("OStype", ConfigurationObjects.toCfgOsType(type), it)
        setProperty("OSversion", version, it)
    }
}
