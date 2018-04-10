package com.nuecho.genesys.cli.models.configuration

import com.genesyslab.platform.applicationblocks.com.CfgObject

data class ConfigurationObjectUpdateResult(
    val status: ConfigurationObjectUpdateStatus,
    val cfgObject: CfgObject
)
