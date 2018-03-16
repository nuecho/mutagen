package com.nuecho.genesys.cli.commands.config.export

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType

interface ExportProcessor {
    fun begin()
    fun beginType(type: CfgObjectType)
    fun processObject(cfgObject: ICfgObject)
    fun endType(type: CfgObjectType)
    fun end()
}
