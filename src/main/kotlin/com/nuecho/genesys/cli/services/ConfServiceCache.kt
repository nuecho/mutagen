package com.nuecho.genesys.cli.services

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.queries.CfgFolderQuery
import com.nuecho.genesys.cli.Logging
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference

object ConfServiceCache : HashMap<ConfigurationObjectReference<*>, ICfgObject>() {
    fun populateCache(confService: IConfService) {
        Logging.debug { "Populating configuration service cache." }

        confService.retrieveMultipleObjects(CfgFolder::class.java, CfgFolderQuery()).forEach {
            ConfServiceCache[it.getReference()] = it
        }

        Logging.debug { "Cached $size configurations objects." }
    }
}
