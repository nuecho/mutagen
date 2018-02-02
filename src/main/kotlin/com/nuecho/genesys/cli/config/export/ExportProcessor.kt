package com.nuecho.genesys.cli.config.export

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.nuecho.genesys.cli.config.ConfigurationObjectType

interface ExportProcessor {
    fun begin()
    fun beginType(type: ConfigurationObjectType)
    fun processObject(type: ConfigurationObjectType, configurationObject: ICfgObject)
    fun endType(type: ConfigurationObjectType)
    fun end()
}
