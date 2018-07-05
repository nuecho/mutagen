package com.nuecho.genesys.cli.services

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.queries.CfgFolderQuery
import com.nuecho.genesys.cli.Logging.debug
import com.nuecho.genesys.cli.getReference
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference

object ConfigurationObjectRepository {
    private val configurationObjects = HashMap<ConfigurationObjectReference<*>, ICfgObject>()

    fun contains(reference: ConfigurationObjectReference<*>) = configurationObjects.containsKey(reference)

    operator fun get(reference: ConfigurationObjectReference<*>) = configurationObjects[reference]

    fun prefetchConfigurationObjects(confService: IConfService) {
        debug { "Prefetching configuration objects." }

        debug { "Prefetching CfgFolder objects." }
        confService.retrieveMultipleObjects(CfgFolder::class.java, CfgFolderQuery()).forEach {
            val reference = it.getReference()
            debug { "Prefetching '$reference'." }
            configurationObjects[reference] = it
        }

        debug { "Prefetched ${configurationObjects.size} configurations objects." }
    }
}
